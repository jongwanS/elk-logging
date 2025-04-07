package com.jw.study.elklogging.presentation;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream cachedContent = new ByteArrayOutputStream();
    private ServletOutputStream outputStream;
    private PrintWriter writer;

    public CachedBodyHttpServletResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("Writer already in use");
        }

        if (outputStream == null) {
            outputStream = new CachedServletOutputStream(cachedContent);
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) {
            throw new IllegalStateException("OutputStream already in use");
        }

        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(cachedContent, getCharacterEncoding()), true);
        }
        return writer;
    }

    /**
     * 클라이언트에 응답 데이터를 실제로 전달
     */
    public void copyBodyToResponse() throws IOException {
        byte[] body = cachedContent.toByteArray();
        getResponse().getOutputStream().write(body);
        getResponse().getOutputStream().flush();
    }

    public byte[] getCachedBody() {
        return cachedContent.toByteArray();
    }

    private static class CachedServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream bos;

        public CachedServletOutputStream(ByteArrayOutputStream bos) {
            this.bos = bos;
        }

        @Override
        public void write(int b) {
            bos.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // Not needed for basic logging
        }
    }
}
