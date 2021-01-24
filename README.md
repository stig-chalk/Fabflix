# Fabflix

Fabflix is a full-stack, end-to-end web-database application, allowing customers to login to browse/search for movie info, add interested movies to their shopping cart, and check out.


The dynamic web pages were designed and implemented with HTML, CSS, and jQuery(JavaScript). 
Fabflix was deployed on AWS EC2 Ubuntu with Apache Tomcat.
There is also a backend MySql database that manages all user data and more than 100k movie records parsed from outside xml sources.


The website uses Ajax to interact with backend Java Servlets to extract/insert data from/to the database via JDBC.
Fabflix also supports fuzzy movie search and suggestion autocomplete, thanks to MySql Full-Text Search and jQuery Autocomplete.
The website also supports pages sorting and pagination, and temporary shopping cart, implemented by session cookies from both ends of the website.

