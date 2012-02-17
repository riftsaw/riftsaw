RiftSaw Project
============


Structures:
===========

conf: this is the folder for configurations, it serves to change the databases for now.


Changing the database:
======================

1）create and install the database driver (for example, mysql):

a) create the folder in $AS7/modules/mysql/main, and then copy the mysql-connector-java*.jar inside it.
b) create a module.xml along with it, the content is as following:

 <module xmlns="urn:jboss:module:1.1" name="mysql">

   <resources>
     <resource-root path="mysql-connector-java-5.1.12.jar"/>
   </resources>
   <dependencies>
     <module name="javax.api"/>
     <module name="javax.transaction.api"/>
   </dependencies>
 </module>

c)add the mysql driver and the datasource in standalone.xml file.

    <subsystem xmlns="urn:jboss:domain:datasources:1.0">
        <datasources>
            .....
            <datasource jndi-name="java:jboss/datasources/BpelDS" enabled="true" use-java-context="true" pool-name="BpelDS">
                <connection-url>jdbc:mysql://localhost:3306/riftsaw</connection-url>
                <driver>mysql</driver>
                <pool/>
                <security>
                    <user-name>root</user-name>
                    <password>jeff</password>
                </security>
            </datasource>
            <drivers>
                ...
                <driver name="mysql" module="mysql">
                    <driver-class>com.mysql.jdbc.Driver</driver-class>
                </driver>
            </drivers>
        </datasources>
    </subsystem>

2) Populate the RiftSaw Schema to the target database.

a) updated the jdbc properties properly in $RiftSaw/conf/jdbc/${database}.properties
b) update the $RiftSaw/conf/build.xml file's database property as your target db, (mysql in this case)
c) run:
         ant create.riftsaw.schema
   to populate the schemas.   (you should be able to view the tables via ant db.show.tables command)

3) Override the properties for bpel component in standalone.xml file.

    <module identifier="org.switchyard.component.bpel" implClass="org.switchyard.component.bpel.deploy.BPELComponent">
        <properties>
            <bpel.db.mode>EXTERNAL</bpel.db.mode>
            <db.emb.create>false</db.emb.create>
            <bpel.db.ext.dataSource>java:jboss/datasources/BpelDS</bpel.db.ext.dataSource>
            <hibernate.dialect>org.hibernate.dialect.MySQLInnoDBDialect</hibernate.dialect>
        </properties>
    </module>

Then you should be ready to fire up the SwitchYard.








