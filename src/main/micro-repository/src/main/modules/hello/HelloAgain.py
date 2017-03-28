# -*- coding: utf-8 -*-
# import sys

global task

host = task.getValue()
task.info('HelloAgain Jython - '+task.getValue())
message = task.getVariables().get("message").getValue()
task.info('['+host+'] : '+'variable '+message)

if ( not task.isConnected()):
    print "    It is not connected "
else:
    grepResolvCOnf = task.sessionExec("grep search /etc/resolv.conf | xargs")
    task.info(grepResolvCOnf)

# print sys.argv

