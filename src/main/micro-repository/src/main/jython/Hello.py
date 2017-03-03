# -*- coding: utf-8 -*-
import sys

from org.mx.ssh import Task
from org.mx.oauth.client import Credential

print '  Hello Jython'
print sys.argv

task = Task("localhost")
credential = Credential("user","password",False)
print "    connecting"
task.connect(credential)

if ( not task.isConnected()):
    print "    It is not connected "
else:
    print "    It is connected"
    javaInstance = task.channelExec("ps -ef | grep java | wc -l")
    print "    ",javaInstance


