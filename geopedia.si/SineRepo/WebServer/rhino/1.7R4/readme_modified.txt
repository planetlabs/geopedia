"js-modified.jar" is a modified version of "js.jar".

Method "buildClassCtor" in ScriptableObject.java is modified in such a way, 
that class methods, which are bridge methods, are skipped.
With the original jar there is a problem, where an overriden method returns 
a subclass of the original method. In some classloaders the original bridge 
methods is then wrongly annotated with the same annotations as the overriden
method. This causes problems with Rhino, so it had to be modified.

Diff patch:


Left file: modified\src\org\mozilla\javascript\ScriptableObject.java
Right file: original\src\org\mozilla\javascript\ScriptableObject.java
1334,1336d1333
<             if (method.isBridge()) {
<             	continue; // skip the bridge methods, which only make problems
<             }
