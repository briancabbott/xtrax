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
# Copyright (c) 2009, 2018, Oracle and/or its affiliates. All rights reserved.
#


import argparse
import os
import tempfile
from opengrok import get_configuration
from indexer import Indexer
from java import get_javaparser
import logging
import sys


"""
 OpenGrok reindexing script for single project. Makes sure it uses
 logging template specific for the project and creates log directory.

"""


def get_logprop_file(logger, template, pattern, project):
    """
    Return the filename of file with logging properties specific for given
    project.
    """

    with open(template, 'r') as f:
        data = f.read()

    data = data.replace(pattern, project)

    with tempfile.NamedTemporaryFile(delete=False) as tmpf:
        tmpf.write(data.encode())

    return tmpf.name


def get_config_file(logger, uri):
    """
    Get fresh configuration from the webapp and store it in temporary file.
    """
    config = get_configuration(logger, uri)

    with tempfile.NamedTemporaryFile(delete=False) as tmpf:
        tmpf.write(config.encode())

    return tmpf.name


if __name__ == '__main__':

    parser = argparse.ArgumentParser(description='OpenGrok indexer wrapper '
                                     'for indexing single project',
                                     parents=[get_javaparser()])
    parser.add_argument('-t', '--template', required=True,
                        help='Logging template file')
    parser.add_argument('-p', '--pattern', required=True,
                        help='Pattern to substitute in logging template with'
                             'project name')
    parser.add_argument('-P', '--project', required=True,
                        help='Project name')
    parser.add_argument('-d', '--directory', required=True,
                        help='Logging directory')
    parser.add_argument('-U', '--uri', default='http://localhost:8080/source',
                        help='URI of the webapp with context path')

    args = parser.parse_args()

    if args.debug:
        logging.basicConfig(level=logging.DEBUG)
    else:
        logging.basicConfig()

    logger = logging.getLogger(os.path.basename(sys.argv[0]))

    # Make sure the log directory exists.
    if not os.path.isdir(args.directory):
        os.makedirs(args.directory)

    # Get files needed for per-project reindex.
    conf_file = get_config_file(logger, args.uri)
    logprop_file = get_logprop_file(logger, args.template, args.pattern,
                                    args.project)

    # Reindex with the modified logging.properties file and read-only config.
    command = []
    command.append('-R')
    command.append(conf_file)
    command.extend(args.options)
    java_opts = []
    if args.java_opts:
        java_opts.extend(args.java_opts)
    java_opts.append("-Djava.util.logging.config.file={}".
                     format(logprop_file))
    indexer = Indexer(command, logger=logger, jar=args.jar,
                      java=args.java, java_opts=java_opts,
                      env_vars=args.environment)
    indexer.execute()
    ret = indexer.getretcode()
    os.remove(conf_file)
    os.remove(logprop_file)
    if ret is None or ret != 0:
        logger.error(indexer.getoutputstr())
        logger.error("Indexer command for project {} failed (return code {})".
                     format(args.project, ret))
        sys.exit(1)
