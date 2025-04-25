#
# This ProGuard configuration file illustrates how to process ProGuard itself.
# Configuration files for typical applications will be very similar.
# Usage:
#     java -jar proguard.jar @proguard.pro
#

# Disregard warnings about missing classes, in case we don't have
# the Ant or J2ME libraries.

-ignorewarnings
-verbose

# Allow methods with the same signature, except for the return type,
# to get the same obfuscation name.

#-overloadaggressively


# Put all obfuscated classes into the nameless root package.

#-defaultpackage 'c'

-keeppackagenames
-keepattributes Signature
-keepattributes Exceptions
-dontoptimize
-dontpreverify


-keep public class * {
    public <fields>;
    public <methods>;
}

-keep class * {
    public <fields>;
    public <methods>;
}


# The main seeds: ProGuard and its companion tool ReTrace.

