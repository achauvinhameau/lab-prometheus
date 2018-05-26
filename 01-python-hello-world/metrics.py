# -*- Mode: Python; python-indent-offset: 4 -*-
#
# Time-stamp: <2018-05-26 17:26:16 alex>
#
# --------------------------------------------------------------------
# lab-prometheus - metrics for prometheus
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
 Prometheus metrics class to store all the metrics, metric flask
 endpoint and decorator for url calls counting.
"""

from prometheus_client import (Summary,
                               Counter,
                               Histogram,
                               generate_latest,
                               CONTENT_TYPE_LATEST )
from flask import Response, request

from ws_app import app

# Create a metric to track time spent and requests made.

PROM_METRICS = {
    "summary": {
        "ws_srv_func_now" : Summary('ws_srv_func_now',
                                    'stats of function now')
    },

    "counter": {
        "endpoint_call": Counter('ws_srv_endpoint_call',
                                 'Number of calls to this url',
                                 ['method', 'endpoint']),

        # business counter
        "ws_srv_is_now_even" : Counter('ws_srv_is_now_even',
                                       'count even now aligned on second',
                                       ['even'])
    }
}


class MetricEndpointCall(object):
    """Class to handle the counting of the calls for a route flask endpoint."""

    def __init__(self, f):
        """Init default method."""
        self.__name__ = f.__name__
        self.f = f

    def __call__(self, *args, **kwargs):
        """Call default method."""
        path = str(request.path)
        verb = request.method
        label_dict = {"method": verb,
                      "endpoint": path}
        PROM_METRICS['counter']['endpoint_call'].labels(**label_dict).inc()
        return self.f(*args, **kwargs)


@app.route('/metrics')
def metrics():
    """Flask endpoint to gather the metrics, will be called by Prometheus."""
    return Response(generate_latest(),
                    mimetype=CONTENT_TYPE_LATEST)
