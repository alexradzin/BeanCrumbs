BeanCrumbs
==========

Annotation processor that extracts Java beans metadata and generates other code. 


The name
--------

The library is called after bread crumbs strewed by [Hansel and Gretel](http://en.wikipedia.org/wiki/Hansel_and_Gretel) from the famous tail recorded by Brothers Grimm. 


What does it do?
----------------

Exactly as bread crumbs leaved by Hansel showed the way back this library helps to create other code based on metadata extracted from original beans. 


Motivation
----------
Bean convention is very popular. Bean is a class with default public constructor and properties. Property consists of a private field and public setter and getter of corresponding type. A lot of tools use reflection to access the fields. Often when we use such tools we refer to specific field using string constant that contains its name. 

For example:

* `BeanUtils.getProperty(person, "name")` when using [Apache Bean Utils](http://commons.apache.org/proper/commons-beanutils/)
* `new Search(Person.class).addFilterEqual("name", "John")` when using [Hibernate Generic DAO](https://code.google.com/p/hibernate-generic-dao/) from [GoogleCode](https://code.google.com/).
*  `@XmlType(propOrder={"name", "lastName"})` when using JAXB annotations. 
*etc*, *etc*. 

The field `name` is declared in class `Person` 3 times: as a field, as a getter and as a setter. Additionally it is probably mentioned multiple times in various places in code as a plain string `"name"`. If sombody decides to change the field `name` to `firstName` he should find all strings "name" and fix them as well. Otherwise the code will be broken. This bug cannot be found at compile time and will appear at runtime only. 

It looks like a good idea to define `final` fields for each property of each bean and use them instead of string literals. However these constants must be maintained too. This is probably the reason that typically people do not define such constants. 

The problem can be solved if these constants are defined automatically. This is the goal of project BeanCrumbs. 


How does it work
----------------
Annotations support that has been introduced in Java 1.5 was extanded in version 1.6 to compile time. We can create annotation processors that are called by compiler during its work. Annotation processors receive information about the structure of class being compiled and can create resources and other classes. BeanCrumbs is a such annotation processor that creates new code. 

For example if source class looks like:

    public class Person {
        private String name;
        private String lastName;
        private Date birthday;
        // setters and getters...
    }

BeanCrumbs will automatically create class:

    public class PersonSkeleton {
        public final static String name = "name";
        public final static String lastName = "lastName";
        public final static String birthday = "birthday";
    }

So we can used these constants instead of string literals:

    import static com.mycompany.PersonSkeleton.name;
    import static com.mycompany.PersonSkeleton.lastName;
    import static com.mycompany.PersonSkeleton.birthday;
    .........
    @XmlType(propOrder={name, lastName, birthday})


If for example `name`  is changed to `firstName` the class `PersonSkeleton` will be re-generated with new field name and all references to `PersonSkeleton.name` will produce compilation error. 


Quick start
-----------
The easiest way to start is to use BeanCrumbs in IDE. BeanCrumbs is destributed as a single jar. 
Download the `beacrumbs.jar` (its actual name depends on current version, e.g. `beancrumbs-1.0.jar`).
Add it to your project. For example in eclipse right click on yor project, choose "Properties", then go to `Java Compiler/Annotation Processor/Factory Path`. Select checkbox "Enable Project specific settings" and add `beancrumbs.jar` using buttons "Add JAR", "Add external JAR" or "Add Variable". Add the jar to project dependencies as well. 

Now mark class you want to process using annotation `@Skeleton`.

That's it. Now try to edit the class to make compiler to work. Probably refresh the project for the first time. See that skeleton is created in the same package. Now try to add some proerty (field, getter and setter) to your class and see that the changes are reflected in skeleton. 


Best practices and configuration
--------------------------------
By default generated code is created in the same package and in the same source folder where the source class is. This is the simplest but not the best way. Creating generated code in special source folder or even in separate project sounds like a good idea. Howerver it requires some additional configuration. 

Create file  under `META-INF/beancrumbs/skeleton.properties`. Add property: `generated.src.dir` to this file. For example for typical maven project where source code is located under `src/main/java` write:

    generated.src.dir=src/generated/java

Save the file and refresh project in IDE. Now all skeletons will be created in configured directory. 

To generate code in separate project add property: `generated.src.project`, e.g.:

    generated.src.project=mygenerated-code
    
The path of project is defined relatively to the current project's parent, i.e. if your project's path is `/user/home/me/proj/myproj` and `generated.src.project=mygenerated-code` the generated code will be created in project located in `/user/home/me/proj/mygenerated-code`.


How to make BeanCrubms to create Skeleton for specific class?
-------------------------------------------------------------
1. The simplest way is to mark class using annotation `@Skeleton` packaged into `beancrumbs.jar`. This means however that application code depends on beancrumbs. 
2. Alternatively you can create skeletons for each class that is already marked using other annotation. For example create skeletons for all persisted entities in the system or for all classes that can be used with JAXB *etc*. To do this just add property `class.annotation` to already mentioned file  `META-INF/beancrumbs/skeleton.properties`, for example:
    
    `class.annotation=javax.persistence.Entity, javax.xml.bind.annotation.XmlRootElement, javax.xml.bind.annotation.XmlType`

3. The classes used for skeleton creation can be specified using additional properties:

    `class.wildcard=com.beanpath.poc.Man?g*`

or even using regular expressions:

    `class.pattern=com.beanpath.poc.Man.g.*`

4. Additionally classes can be specified using `META-INF/beancrumbs/skeleton.index` that contains list of fully quailified class names. Each class name should be written in separate line. 
    
    
    
    
