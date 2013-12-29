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
