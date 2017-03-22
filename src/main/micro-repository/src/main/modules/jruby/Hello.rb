require 'java'

java_import java.lang.System
version = System.getProperties["java.runtime.version"]

#org.apache.log4j.BasicConfigurator.configure
#log = org.apache.log4j.Logger.getLogger "jruby"
#log.info("  Hello from LogFactory")

puts "  Hello from JRuby"
puts "  Java version "+version

System.out.println("  Hello JRuby from System")

def puts(value)
  System.out.println(value)
end

puts "  Hello puts overwrite"
