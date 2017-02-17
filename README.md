Java Servlet adding a google cloud user to view another projects image files.

Specify service file (account that are owner of images) in `resources/project.properties`

The service file user need to be "owner" cloudpanel->IAM & admin->IAM->make sure account is owner (from roles dropdown -> project->owner)

You need to enable this API: (cloudpanel->APIs) Google Cloud Resource Manager API

`mvn jetty:run`

To add an user email:  `http://localhost:8080/ImageAuthServlet?add=username@gmail.comm`
