# -*- coding: utf-8 -*-
import sys
from org.mx.ssh import Task
from org.mx.oauth.client import Credential
from com.jcraft.jsch import ProxySOCKS5

global variableMap

print '    Hello Jython'

proxy = ProxySOCKS5("localhost", 8889);

hosts = variableMap.get("hosts")
for host in hosts:

    task = Task(host)
    task.setProxySOCKS5(proxy)

    credential = Credential("user","password",False)
    print "    It is connecting on", host
    task.connect(credential)

    if ( not task.isConnected()):
        print "    It is not connected "
    else:
        grepResolvCOnf = task.channelExec("grep search /etc/resolv.conf | xargs")
        print "   ",grepResolvCOnf
