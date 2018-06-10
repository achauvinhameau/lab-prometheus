# -*- Mode: Python; python-indent-offset: 4 -*-
#
# Time-stamp: <2018-05-31 16:24:45 alex>
#
# --------------------------------------------------------------------
# lab-prometheus - basic python instrumentation for prometheus.
#
# Copyright (C) 2016-2017  Alexandre Chauvin Hameau <ach@meta-x.org>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# --------------------------------------------------------------------

"""
lab-prometheus - basic python instrumentation for prometheus linked to consul

virtualenv lab03
source lab03/bin/activate
pip install -r requirements.txt
python ./srv.py %port%

get some ws on /now counting the even and odd epoch values
get the ws with a client on /metrics and check ws_srv_is_now_even counter
"""

import datetime
import sys
import signal

from metrics import PROM_METRICS, MetricEndpointCall
from flask import make_response, jsonify
from ws_app import app
import consul

iServicePort = 5000

if len(sys.argv) > 1:
    try:
        iServicePort = int(sys.argv[1])
    except Exception:
        exit()

CONSUL = consul.Consul()

# register self as a service
serviceChecker = consul.Check.http(url="http://127.0.0.1:{}/_checker".format(iServicePort),
                                   interval="10s",
                                   timeout="1s",
                                   deregister="1m")

CONSUL.agent.service.register("pyTest",
                              service_id="pyTest-{}".format(iServicePort),
                              port=iServicePort,
                              check=serviceChecker)

COUNT_FCT_NOW = PROM_METRICS['summary']['ws_srv_func_now']

@app.route('/now')
@MetricEndpointCall
@COUNT_FCT_NOW.time()
def ws_now():
    """Now service to get date time on the server."""
    now = datetime.datetime.now()

    # business counter
    if int(now.timestamp()) % 2 == 0:
        PROM_METRICS['counter']['ws_srv_is_now_even'].labels(even='yes').inc()
    else:
        PROM_METRICS['counter']['ws_srv_is_now_even'].labels(even='no').inc()

    return make_response(jsonify({
        'epoch': now.timestamp(),
        'ctime': now.ctime(),
        'date': str(now)
    }), 200)


@app.route('/_checker')
def ws_checker():
    return "ok"


# -----------------------------------------
def trap_signal(sig, _):
    """Exit signal for clean derigister from consul."""
    CONSUL.agent.service.deregister(service_id="pyTest-{}".format(iServicePort))
    exit()

signal.signal(signal.SIGTERM, trap_signal)
signal.signal(signal.SIGINT, trap_signal)

if __name__ == '__main__':
    app.secret_key = "lab-02-consul"
    app.debug = False

    app.run(host='0.0.0.0', port=iServicePort)
