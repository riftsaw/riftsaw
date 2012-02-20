RiftSaw Database Scripts:
=========================

This is the module that provides the RiftSaw 3.x's various database scripts, and the ANT script for creating and removing the database scripts from target database.


Changing the database:
======================

1）create and install the database driver (for example, mysql):
--------------------------------------------------------------

a) create the folder in $AS7/modules/mysql/main, and then copy the mysql-connector-java-5.1.12.jar (of course, you can choose other versions) inside it.
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

2 Populate the RiftSaw Schema to the target database:
-----------------------------------------------------

a) updated the jdbc properties properly in jdbc/${database}.properties
b) update the build.xml file's database property as your target db, (mysql in this case)
c) run following command to populate the schema.
     ant create.riftsaw.schema

Note: you should be able to view the tables via: ant db.show.tables command, and drop the database script through: ant drop.riftsaw.schema

3 Override the properties for bpel component in standalone.xml file:
--------------------------------------------------------------------

Go to the $JBoss-AS7/standalone/configuration/standalone.xml, find the BPELComponent, and then overriding following properties.

  <module identifier="org.switchyard.component.bpel" implClass="org.switchyard.component.bpel.deploy.BPELComponent">
      <properties>
          <bpel.db.mode>EXTERNAL</bpel.db.mode>
          <db.emb.create>false</db.emb.create>
          <bpel.db.ext.dataSource>java:jboss/datasources/BpelDS</bpel.db.ext.dataSource>
          <hibernate.dialect>org.hibernate.dialect.MySQLInnoDBDialect</hibernate.dialect>
      </properties>
  </module>

Then you should be ready to start the SwitchYard, the BPEL component will use the database that you created.
