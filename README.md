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
*etc*, *etc*. 

The field `name` is declared in class `Person` 3 times: as a field, as a getter and as a setter. Additionally it is probably mentioned multiple times in various places in code as a plain string `"name"`. If sombody decides to change the field `name` to `firstName` he should find all strings "name" and fix them as well. Otherwise the code will be broken. This bug cannot be found at compile time and will appear at runtime only. 

It looks like a good idea to define `final` fields for each property of each bean and use them instead of string literals. However these constants must be maintained too. This is probably the reason that typically people do not define such constants. 

The problem can be solved if these constants are defined automatically. This is the goal of project BeanCrumbs. 


How does it work
----------------


Quick start
-----------
