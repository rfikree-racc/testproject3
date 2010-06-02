package sr.projectx.web;

import java.io.IOException;
import java.io.Writer;

import javax.faces.bean.ManagedProperty;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sr.projectx.entities.User;
import sr.projectx.services.UserService;

public class UserServlet extends HttpServlet {
	
	private static final long serialVersionUID = 4701709787440016631L;
	
	@ManagedProperty(value = "#{userService}")
	private UserService userService;

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		Writer writer = response.getWriter();
		
		User user = userService.retrieve("admin");
		if (user != null) {
			writer.write(String.format("<b>User found: '%s'</b>", user));
		} else {
			user = new User();
			user.setUsername("admin");
			user.setPassword("admin");
			user.setEmail("admin@projectx.sr");
			try {
			    userService.create(user);
		        writer.write("<b>User created</b>");
			} catch (Exception e) {
			    writer.write("<b>Could not create user</b>");
			}
		}
	}

}