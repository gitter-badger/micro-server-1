# -*- coding: utf-8 -*-
# import sys

global task

task.info('HelloAgain Jython - '+task.getValue())
message = task.getVariables().get("message").getValue()
task.info('Message : '+message)

# print sys.argv

