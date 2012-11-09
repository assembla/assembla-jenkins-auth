Jenkins Assembla OAuth Plugin
============================

Read more: [http://wiki.jenkins-ci.org/display/JENKINS/Assembla+OAuth+Plugin](http://wiki.jenkins-ci.org/display/JENKINS/Assembla+OAuth+Plugin)

Overview
--------
This plugin enables OAuth (see http://oauth.net) authentication for Assembla (http://www.assembla.com) users.
Assembla users are allowed to be authenticated to Jenkins instance using their Assembla credentials and delegate the authorization to Assembla.

A. Assembla Security Realm (authentication):
--------------------------------------------

Handles the authentication and acquisition of the Assembla OAuth token for the connecting user.
Takes the client id (API key) and client secret (API key secret) from the application registration here:
https://www.assembla.com/user/edit/manage_clients

The entry should look like this:
	Assembla API uri:   https://api.assembla.com
	Main url:           http://127.0.0.1:8080
	Callback url:       http://127.0.0.1:8080/securityRealm/finishLogin
	Space url name:     assembla_space_name

Assembla API uri: by default is https://api.assembla.com. Do not change if you are not running a private install of Assembla.

Main url and Callback url: use the url of your jenkins instance instead of http://127.0.0.1:8080

Space url name: has to be set for authorization purposes, replace assembla_space_name with your space's url name (www.assembla.com/spaces/<space_url_name>)

B. Assembla Authorization Strategy (authorization):
---------------------------------------------------

User Jenkins permissions are based on Assembla permission:
	Assembla user with ALL permission for space has Jenkins ADMIN access.
	Assembla user with EDIT permission for space has Jenkins EDIT access.
	Assembla user with VIEW permission for space has Jenkins READ access.
	Assembla user with NONE permission for space has NO Jenkins access.

"Admin usernames" field enables to put coma separated usernames. These users will:

- have Jenkins ADMIN access
- have external access (access to Jenkins API via basic http auth) using their username and API key defined in Jenkins People section: http://127.0.0.1:8080/user/<user>/configure

License
-------

	(The MIT License)

	Copyright (c) 2011 Michael O'Cleirigh

	Permission is hereby granted, free of charge, to any person obtaining
	a copy of this software and associated documentation files (the
	'Software'), to deal in the Software without restriction, including
	without limitation the rights to use, copy, modify, merge, publish,
	distribute, sublicense, and/or sell copies of the Software, and to
	permit persons to whom the Software is furnished to do so, subject to
	the following conditions:

	The above copyright notice and this permission notice shall be
	included in all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND,
	EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
	MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
	IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
	CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
	TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
	SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

