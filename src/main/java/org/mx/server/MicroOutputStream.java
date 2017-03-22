package org.mx.server;

/*

  Copyright 2004-2012, Martian Software, Inc.

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


import java.io.IOException;

class MicroOutputStream extends java.io.DataOutputStream {

    private final Object lock;
    private byte streamCode;
    private boolean closed = false;

    public MicroOutputStream(java.io.OutputStream out, byte streamCode) {
        super(out);
        this.lock = out;
        this.streamCode = streamCode;
    }

    public void write(byte[] b) throws IOException {
        throwIfClosed();
        write(b, 0, b.length);
    }

    public void write(int b) throws IOException {
        throwIfClosed();
        byte[] b2 = {(byte) b};
        write(b2, 0, 1);
    }

    public void write(byte[] b, int offset, int len) throws IOException {
        throwIfClosed();
        synchronized(lock) {
            writeInt(len);
            writeByte(streamCode);
            out.write(b, offset, len);
        }
        flush();
    }

    public void close() throws IOException {
        throwIfClosed();
        closed = true;
    }

    public void flush() throws IOException {
        throwIfClosed();
        super.flush();
    }

    private void throwIfClosed() throws IOException {
        if(closed) {
            throw new IOException();
        }
    }
}
