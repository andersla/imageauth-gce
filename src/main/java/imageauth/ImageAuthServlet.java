package imageauth;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.cloud.AuthCredentials;
import com.google.cloud.Identity;
import com.google.cloud.resourcemanager.Policy;
import com.google.cloud.resourcemanager.Policy.ProjectRole;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;

/**
 * 
 * @author anders
 * 
 * the service file user need to be "owner" cloudpanel->IAM & admin->IAM->make sure account is owner (from roles dropdown -> project->owner)
 * 
 * need to enable this API (cloudpanel->APIs): Google Cloud Resource Manager API
 *
 * to add an user email:  http://localhost:8080/ImageAuthServlet?add=username@gmail.comm
 *
 */

public class ImageAuthServlet extends HttpServlet {

	
	private static Properties props = new Properties();
	static{
		Locale.setDefault(Locale.ENGLISH);
		Logger.getRootLogger().setLevel(Level.INFO);
    	try {
			props.load(ImageAuthServlet.class.getResourceAsStream("/project.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static final Logger logger = Logger.getLogger(ImageAuthServlet.class);

	/** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws Exception 
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			request.setCharacterEncoding("UTF-8");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			
			if(request.getParameter("add") != null && request.getParameter("add").length() > 0){
				String newUser = request.getParameter("add");
				
				addUser(newUser);
				
				logger.info("user: " + newUser + " added ok");
				out.append("user added ok");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
		
		response.flushBuffer();

	}

	private void addUser(String newImageUser) throws Exception{
			
			try {
				// Get default resource manager with json credentials file specifyed in GOOGLE_APPLICATION_CREDENTIALS
				// ResourceManager resourceManager = ResourceManagerOptions.defaultInstance().service();
				
				// Get resource manager with custom credentials file
				String pathToJsonCredentials = props.getProperty("keyfile");
				ResourceManager resourceManager = ResourceManagerOptions.builder()
						.authCredentials(AuthCredentials.createForJson(new FileInputStream(pathToJsonCredentials)))
						.build()
						.service();
				
				// Get project
				String projectId = props.getProperty("projectid"); 
				Project project = resourceManager.get(projectId);
				
//				Iterator<Project> projectIterator = resourceManager.list().iterateAll();
//				System.out.println("Projects I can view:");
//				while (projectIterator.hasNext()) {
//				  System.out.println(projectIterator.next().projectId());
//				}
				
				// Get the project's policy
				Policy policy = project.getPolicy();

				// Add a image user by modifying the current policy
				Policy.Builder modifiedPolicy = policy.toBuilder();
				Identity newViewer = Identity.user(newImageUser);
				modifiedPolicy.addIdentity("roles/compute.imageUser", newViewer);

				project.replacePolicy(modifiedPolicy.build());
				
			} catch (Exception e) {
				logger.error("Error adding user", e);
				throw e;
			}	
	}


	/** Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/** Handles the HTTP <code>POST</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/** Returns a short description of the servlet.
	 */
	public String getServletInfo() {
		return "Short description";
	}
}