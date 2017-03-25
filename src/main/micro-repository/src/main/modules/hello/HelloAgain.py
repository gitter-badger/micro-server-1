# -*- coding: utf-8 -*-
# import sys

global task

host = task.getValue()
task.info('HelloAgain Jython - '+task.getValue())
message = task.getVariables().get("message").getValue()
task.info('['+host+'] : '+'variable '+message)

# print sys.argv

