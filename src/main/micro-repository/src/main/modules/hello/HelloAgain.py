# -*- coding: utf-8 -*-
import sys

global task

print '    HelloAgain Jython'
message = task.getVariables().get("message").getValue()
task.info('    Message : '+message)
# print sys.argv

