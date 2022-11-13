# WOV backend
## TODO
- Mail confirmation to create account
- Shop shit
- Find a better solution for GMP
- Set "Bearer" prefix for return token on login

## NOTES
- Jar has to be named "API.war" with capital letters
- Tomcat has to be restarted when a new jar is uploaded
- Need to manually upload src/main/resources/application-prod.properties to server


## RUNNING LOCALLY (One time setup) WIP


INSTALL SQL & GMP 
1: Logging into mysql
```bash
sudo mysql -u root
```

2: Creating Mysql user
```mysql
CREATE USER 'spring'@'localhost' IDENTIFIED BY 'Q%Z52e!';
exit
```

3: Installing GMP
```bash
sudo cp ./lib/libjcl.so /lib                                                                                                                     1 ↵
sudo cp ./lib/libnativegmp.so /lib 
sudo chown root:root  /lib/libjcl.so
sudo chown root:root  /lib/libnativegmp.so 
sudo chmod 755  /lib/libjcl.so 
sudo chmod 755  /lib/libnativegmp.so 

```
Not sure if needed anymore
```sql
GRANT ALL  PRIVILEGES ON acore_custom.* TO 'spring'@'localhost';
GRANT ALL  PRIVILEGES ON acore_auth.* TO 'spring'@'localhost';
GRANT ALL  PRIVILEGES ON acore_characters.* TO 'spring'@'localhost';
GRANT ALL  PRIVILEGES ON acore_world.* TO 'spring'@'localhost';
```