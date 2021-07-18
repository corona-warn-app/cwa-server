Allowing both safe and unsafe HTTP methods is security-sensitive
Security Hotspot
Minor
Main sources
cwe, owasp-a5, sans-top25-insecure, spring
Available Since
Jun 28, 2018
SonarQube (Java)
Constant/issue: 5min
An HTTP method is safe when used to perform a read-only operation, such as retrieving information. 
In contrast, an unsafe HTTP method is used to change the state of an application, 
for instance to update a user’s profile on a web application.

Common safe HTTP methods are GET, HEAD, or OPTIONS.

Common unsafe HTTP methods are POST, PUT and DELETE.

Allowing both safe and unsafe HTTP methods to perform a specific operation on a web application could impact its security,
for example CSRF protections are most of the time only protecting operations performed by unsafe HTTP methods.

Ask Yourself Whether
HTTP methods are not defined at all for a route/controller of the application.
Safe HTTP methods are defined and used for a route/controller that can change the state of an application.
There is a risk if you answered yes to any of those questions.

Recommended Secure Coding Practices
For all the routes/controllers of an application,
the authorized HTTP methods should be explicitly defined and safe HTTP methods

 should only be used to perform read-only operations.

Sensitive Code Example
@RequestMapping("/delete_user")  // 
Sensitive:
 by default all HTTP methods are allowed
public String delete1(String username) {
* // state of the application will be changed here
}

@RequestMapping(path = "/delete_user", method = {RequestMethod.GET, RequestMethod.POST}) 
* // Sensitive: both safe and unsafe methods are allowed
String delete2(@RequestParam("id") String id) {
* // state of the application will be changed here
}
Compliant Solution
@RequestMapping("/delete_user", method = RequestMethod.POST)  // Compliant
public String delete1(String username) {
* // state of the application will be changed here
}

@RequestMapping(path = "/delete_user", method = RequestMethod.POST) // Compliant
String delete2(@RequestParam("id") String id) {
* // state of the application will be changed here
}
See
OWASP Top 10 2017 Category A5 - Broken Access Control
MITRE, CWE-352 - Cross-Site Request Forgery (CSRF)
OWASP: Cross-Site Request Forgery
SANS Top 25 - Insecure Interaction Between Components
Spring Security Official Documentation: Use proper HTTP verbs (CSRF protection)
Quality Profiles
Sonar way
BUILT-IN
Minor		
