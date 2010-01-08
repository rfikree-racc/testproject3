package org.ozsoft.webdav.server.method;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.ozsoft.webdav.Depth;
import org.ozsoft.webdav.PropStat;
import org.ozsoft.webdav.WebDavConstants;
import org.ozsoft.webdav.WebDavException;
import org.ozsoft.webdav.WebDavResponse;
import org.ozsoft.webdav.WebDavStatus;
import org.ozsoft.webdav.server.WebDavBackend;

/**
 * WebDAV PROPFIND method.
 * 
 * @author Oscar Stigter
 */
public class PropFindMethod extends AbstractMethod {

	/** Method name. */
	private static final String NAME = "PROPFIND";
	
	/** Creation date format. */
	private static final DateFormat CREATION_DATE_FORMAT =
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

	/** Last modification date format. */
	private static final DateFormat LASTMODIFIED_DATE_FORMAT =
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

	/** Logger. */
	private static final Logger LOG = Logger.getLogger(PropFindMethod.class);

	/**
	 * Constructor.
	 * 
	 * @param servletContext
	 *            The servlet context.
	 * @param backend
	 *            The WebDAV backend.
	 */
	public PropFindMethod(String servletContext, WebDavBackend backend) {
		super(NAME, servletContext, backend);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ozsoft.webdav.server.method.AbstractMethod#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void process(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		super.process(request, response);
		String uri = getUriFromRequest(request);
		Depth depth = getDepth(request);
		List<WebDavResponse> responses = new ArrayList<WebDavResponse>();
		String requestBody = getRequestBody(request);
		try {
			if (requestBody.length() == 0) {
				// No request body, so handle as 'allprop' type.
				doPropfindAllprop(uri, depth, responses);
			} else {
				// Parse request body.
				Document doc = DocumentHelper.parseText(requestBody);
				Element root = doc.getRootElement();
				if (root.getNamespaceURI().equals(WebDavConstants.DAV_NS)
						&& root.getName().equals("propfind")) {
					List<?> children = root.elements();
					if (children.size() == 1) {
						Element typeElement = (Element) children.get(0);
						String typeName = typeElement.getName();
						if (typeName.equals("prop")) {
							// Request for specific set of properties.
							List<String> propNames = new ArrayList<String>();
							for (Object obj : typeElement.elements()) {
								if (obj instanceof Element) {
									Element el = (Element) obj;
									// TODO: Handle request for non-WebDAV properties.
									if (el.getNamespaceURI().equals(
											WebDavConstants.DAV_NS)) {
										propNames.add(el.getName());
									}
								} else {
									// TODO: Child of 'prop' element not an element.
								}
							}
							if (propNames.size() == 0) {
								// TODO: Invalid request; no (WebDAV) properties specified.
							} else {
								doPropfindProp(uri, depth, propNames, responses);
							}
						} else if (typeName.equals("allprop")) {
							// Request for all properties (incl. values).
							doPropfindAllprop(uri, depth, responses);
						} else if (typeName.equals("propname")) {
							// Request for all property names.
							doPropfindPropname(uri, depth, responses);
						} else {
							// TODO: Invalid request; invalid child element of 'propfind' element.
						}
					} else {
						// TODO: Invalid request; 'propfind' element has invalid number of child elements.
					}
				} else {
					// TODO: Invalid request; no 'propfind' element.
				}
			}

			StringBuilder sb = new StringBuilder();
			sb.append(WebDavConstants.XML_DECLARATION);
			sb.append("<multistatus xmlns=\"").append(WebDavConstants.DAV_NS)
					.append("\">\n");
			for (WebDavResponse r : responses) {
				sb.append("  <response>\n");
				String href = servletContext + r.getUri();
				if (href.endsWith("/")) {
					href = href.substring(0, href.length() - 1);
				}
				sb.append("    <href>").append(href).append("</href>\n");
				for (PropStat propStat : r.getPropStats()) {
					sb.append("    <propstat>\n");
					sb.append("      <prop>\n");
					String propName = propStat.getName();
					sb.append("        <").append(propName).append(">");
					String propValue = propStat.getValue();
					if (propValue != null) {
						sb.append(propValue);
					}
					sb.append("</").append(propName).append(">\n");
					sb.append("      </prop>\n");
					WebDavStatus status = propStat.getStatus();
					sb.append("      <status>HTTP/1.1 ").append(
							status.getCode());
					sb.append(' ').append(status.getName()).append(
							"</status>\n");
					sb.append("    </propstat>\n");
				}
				sb.append("  </response>\n");
			}
			sb.append("</multistatus>\n");
			String responseBody = sb.toString();
			if (LOG.isTraceEnabled()) {
				LOG.trace("Response:\n" + responseBody);
			}
			response.getWriter().write(responseBody);
		} catch (DocumentException e) {
			// TODO: Invalid XML request body.
			LOG.error("Could not parse PROPFIND request", e);
		} catch (WebDavException e) {
			LOG.error(String.format("Error listing collection '%s'", uri), e);
		}

		// Response status and headers.
		response.setStatus(WebDavStatus.MULTI_STATUS.getCode());
		response.setContentType(WebDavConstants.CONTENT_TYPE);
	}

	/**
	 * Handles a PROPFIND 'prop' request.
	 * 
	 * @param uri
	 *            The resource URI.
	 * @param depth
	 *            The Depth header value.
	 * @param propNames
	 *            The property names.
	 * @param responses
	 *            The property responses.
	 */
	private void doPropfindProp(String uri, Depth depth,
			List<String> propNames, List<WebDavResponse> responses)
			throws ServletException, IOException, WebDavException {
		if (backend.exists(uri)) {
			doPropfindProp(uri, propNames, responses);
			if (backend.isCollection(uri)) {
				if (depth != Depth.ZERO) {
					String[] children = backend.getChildrenNames(uri);
					for (String child : children) {
						String childUri = (uri.endsWith("/")) ? uri + child
								: uri + "/" + child;
						if (depth == Depth.ONE) {
							doPropfindProp(childUri, propNames, responses);
						} else {
							doPropfindProp(childUri, depth, propNames,
									responses);
						}
					}
				}
			}
		} else {
			// TODO: Resource not found.
		}
	}

	/**
	 * Handles a PROPFIND 'prop' request for a specific resource.
	 * 
	 * @param uri
	 *            The resource URI.
	 * @param propNames
	 *            The property names.
	 * @param responses
	 *            The resource responses.
	 */
	private void doPropfindProp(String uri, List<String> propNames,
			List<WebDavResponse> responses) throws ServletException,
			IOException, WebDavException {
		if (backend.exists(uri)) {
			WebDavResponse response = new WebDavResponse(uri);
			for (String propName : propNames) {
				PropStat propStat = new PropStat(propName);
				if (propName.equals(WebDavConstants.DISPLAYNAME)) {
					// Display name.
					propStat.setValue(getResourceName(uri));
					propStat.setStatus(WebDavStatus.OK);
				} else if (propName.equals(WebDavConstants.RESOURCETYPE)) {
					// Resource type.
					if (backend.isCollection(uri)) {
						propStat.setValue(WebDavConstants.COLLECTION);
					} else {
						propStat.setValue("");
					}
					propStat.setStatus(WebDavStatus.OK);
				} else if (propName.equals(WebDavConstants.CONTENT_TYPE)) {
					// Content type.
					if (!backend.isCollection(uri)) {
						propStat.setValue(backend.getContentType(uri));
						propStat.setStatus(WebDavStatus.OK);
					} else {
						propStat.setStatus(WebDavStatus.NOT_FOUND);
					}
				} else if (propName.equals(WebDavConstants.GETCONTENTLENGTH)) {
					// Content length.
					if (!backend.isCollection(uri)) {
						propStat.setValue(String.valueOf(backend
								.getContentLength(uri)));
						propStat.setStatus(WebDavStatus.OK);
					} else {
						propStat.setStatus(WebDavStatus.NOT_FOUND);
					}
				} else if (propName.equals(WebDavConstants.CREATIONDATE)) {
					// Creation date in ISO date format.
					Date date = backend.getCreated(uri);
					propStat.setValue(CREATION_DATE_FORMAT.format(date));
					propStat.setStatus(WebDavStatus.OK);
				} else if (propName.equals(WebDavConstants.GETLASTMODIFIED)) {
					// Last modification date in internet date format.
					Date date = backend.getModified(uri);
					propStat.setValue(LASTMODIFIED_DATE_FORMAT.format(date));
					propStat.setStatus(WebDavStatus.OK);
				} else {
					// Unknown property.
					propStat.setStatus(WebDavStatus.NOT_FOUND);
				}
				response.addPropStat(propStat);
			}
			responses.add(response);
		} else {
			// TODO: Resource not found.
		}
	}

	/**
	 * Handles a PROPFIND 'allprop' request.
	 * 
	 * @param uri
	 *            The resource URI.
	 * @param depth
	 *            The Depth header value.
	 * @param responses
	 *            The property responses.
	 */
	private void doPropfindAllprop(String uri, Depth depth, List<WebDavResponse> responses)
			throws ServletException, IOException, WebDavException {
		List<String> propNames = new ArrayList<String>();
		propNames.add(WebDavConstants.DISPLAYNAME);
		propNames.add(WebDavConstants.RESOURCETYPE);
		propNames.add(WebDavConstants.GETCONTENTLENGTH);
		propNames.add(WebDavConstants.CREATIONDATE);
		propNames.add(WebDavConstants.GETLASTMODIFIED);
		doPropfindProp(uri, propNames, responses);
	}

	/**
	 * Handles a PROPFIND 'propname' request.
	 * 
	 * @param uri
	 *            The resource URI.
	 * @param depth
	 *            The Depth header value.
	 * @param responses
	 *            The property responses.
	 */
	private void doPropfindPropname(String uri, Depth depth, List<WebDavResponse> responses)
			throws ServletException, IOException {
		// TODO: PROPFIND by name
	}

}
