package org.mx.server;

import java.io.IOException;
import java.net.InetAddress;

/*

 Copyright 2016, MXDeploy, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */

/**
 * Represents the address on which the MXDeploy server listens.
 */
public class ListeningAddress {
  private InetAddress inetAddress;
  private int port;

  /**
   * Constructs a listening address for an internet address and port.
   */
  public ListeningAddress(String inetAddress, int port) throws IOException {
    this.port = port;
    this.inetAddress = InetAddress.getByName(inetAddress);
  }

  /**
   * Returns the listening internet address 
   */
  public InetAddress getPublicInetAddress() {
    return this.inetAddress;
  }

  /**
   * Returns the listening internet port 
   */
  public int getPort() {
    return this.port;
  }


}
