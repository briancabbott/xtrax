#!/usr/bin/env python3

# CDDL HEADER START
#
# The contents of this file are subject to the terms of the
# Common Development and Distribution License (the "License").
# You may not use this file except in compliance with the License.
#
# See LICENSE.txt included in this distribution for the specific
# language governing permissions and limitations under the License.
#
# When distributing Covered Code, include this CDDL HEADER in each
# file and include the License file at LICENSE.txt.
# If applicable, add the following below this CDDL HEADER, with the
# fields enclosed by brackets "[]" replaced with your own identifying
# information: Portions Copyright [yyyy] [name of copyright owner]
#
# CDDL HEADER END

#
# Copyright (c) 2008, 2018, Oracle and/or its affiliates. All rights reserved.
# Portions Copyright (c) 2017-2018, Chris Fraire <cfraire@me.com>.
#

import platform
from command import Command
from utils import is_exe
import os
import argparse
import sys
import logging


class Java(Command):
    """
    java executable wrapper class
    """

    def __init__(self, command, logger=None, main_class=None, java=None,
                 jar=None, java_opts=None, classpath=None, env_vars=None,
                 redirect_stderr=True):

        if not java:
            java = self.FindJava(logger)
            if not java:
                raise Exception("Cannot find Java")

        if not is_exe(java):
            raise Exception("{} is not executable file".format(java))

        logger.debug("Java = {}".format(java))

        java_command = [java]
        if java_opts:
            java_command.extend(java_opts)
        if classpath:
            java_command.append('-classpath')
            java_command.append(classpath)
        if jar:
            java_command.append('-jar')
            java_command.append(jar)
        if main_class:
            java_command.append(main_class)
        env = None
        if env_vars:
            env = {}
            for spec in env_vars:
                if spec.find('=') != -1:
                    name, value = spec.split('=')
                    env[name] = value

        java_command.extend(command)
        logger.debug("Java command: {}".format(java_command))

        super().__init__(java_command, logger=logger, env_vars=env,
                         redirect_stderr=redirect_stderr)

    def FindJava(self, logger):
        """
        Determine Java home directory based on platform.
        """
        java = None
        system_name = platform.system()
        if system_name == 'SunOS':
            rel = platform.release()
            if rel == '5.10':
                javaHome = "/usr/jdk/instances/jdk1.7.0"
            elif rel == '5.11':
                javaHome = "/usr/jdk/latest"
            java = os.path.join(javaHome, 'bin', 'java')
        elif system_name == 'Darwin':
            cmd = Command('/usr/libexec/java_home')
            cmd.execute()
            java = os.path.join(cmd.getoutputstr(), 'bin', 'java')
        elif system_name == 'Linux':
            link_path = '/etc/alternatives/java'
            if os.path.exists(link_path):
                # Resolve the symlink.
                java = os.path.realpath(link_path)

        return java


def get_javaparser():
    parser = argparse.ArgumentParser(add_help=False)
    parser.add_argument('-D', '--debug', action='store_true',
                        help='Enable debug prints')
    parser.add_argument('-j', '--java',
                        help='path to java binary')
    parser.add_argument('-J', '--java_opts',
                        help='java options', action='append')
    parser.add_argument('-e', '--environment', action='append',
                        help='Environment variables in the form of name=value')

    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument('-a', '--jar',
                       help='Path to jar archive to run')
    group.add_argument('-c', '--classpath',
                       help='Class path')

    parser.add_argument('options', nargs='+', help='options')

    return parser


if __name__ == '__main__':

    parser = argparse.ArgumentParser(description='java wrapper',
                                     parents=[get_javaparser()])
    parser.add_argument('-m', '--mainclass', required=True,
                        help='Main class')

    args = parser.parse_args()

    if args.debug:
        logging.basicConfig(level=logging.DEBUG)
    else:
        logging.basicConfig()

    logger = logging.getLogger(os.path.basename(sys.argv[0]))

    java = Java(args.options, logger=logger, java=args.java,
                jar=args.jar, java_opts=args.java_opts,
                classpath=args.classpath, main_class=args.mainclass,
                env_vars=args.environment)
    java.execute()
    ret = java.getretcode()
    if ret is None or ret != 0:
        logger.error(java.getoutputstr())
        logger.error("java command failed (return code {})".format(ret))
        sys.exit(1)
