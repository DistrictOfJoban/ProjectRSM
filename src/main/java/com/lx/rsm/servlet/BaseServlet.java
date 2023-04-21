package com.lx.rsm.servlet;

import com.google.gson.JsonArray;
import com.lx.rsm.data.DiffManager;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@WebServlet(asyncSupported = true)
public class BaseServlet extends HttpServlet {
    public static final HashMap<Long, DiffManager> diffManagerCache = new HashMap<>();
    protected static final int RADIUS_THRESHOLD = 2000;
    protected static JsonArray getXZObject(BlockPos pos) {
        final JsonArray posArray = new JsonArray();
        posArray.add(pos.getX());
        posArray.add(pos.getZ());
        return posArray;
    }
    protected static JsonArray getPosObject(Vec3d pos) {
        final JsonArray posArray = new JsonArray();
        posArray.add(pos.x);
        posArray.add(pos.y);
        posArray.add(pos.z);
        return posArray;
    }

    protected static JsonArray getPosObject(BlockPos pos) {
        final JsonArray posArray = new JsonArray();
        posArray.add(pos.getX());
        posArray.add(pos.getY());
        posArray.add(pos.getZ());
        return posArray;
    }

    protected static JsonArray getCornerObject(Pair<Integer, Integer> pair) {
        final JsonArray posArray = new JsonArray();
        posArray.add(pair.getLeft());
        posArray.add(pair.getRight());
        return posArray;
    }

    protected long getUniquePosId(BlockPos pos1, BlockPos pos2) {
        return Math.abs(pos1.asLong()) + Math.abs(pos2.asLong());
    }

    protected static void sendResponse(HttpServletResponse response, AsyncContext asyncContext, String content) {
        final ByteBuffer contentBytes = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));
        try {
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Content-Type", "application/json");
            final ServletOutputStream servletOutputStream = response.getOutputStream();
            servletOutputStream.setWriteListener(new WriteListener() {
                @Override
                public void onWritePossible() throws IOException {
                    while (servletOutputStream.isReady()) {
                        if (!contentBytes.hasRemaining()) {
                            response.setStatus(200);
                            asyncContext.complete();
                            return;
                        }
                        servletOutputStream.write(contentBytes.get());
                    }
                }

                @Override
                public void onError(Throwable t) {
                    asyncContext.complete();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void startSSE(HttpServletRequest request, HttpServletResponse response, int intervalInMs, boolean continous, SSECallback callback) {
        AsyncContext asyncContext = request.startAsync(request, response);

        /* Parse location specific parameters */
        final String cidStr = request.getParameter("cid");
        final String locX = request.getParameter("x");
        final String locZ = request.getParameter("z");
        int x = 0;
        int z = 0;
        long cid = 0;

        try {
            x = Integer.parseInt(locX);
            z = Integer.parseInt(locZ);
            cid = Long.parseLong(cidStr);
        } catch (Exception ignored) {
        }
        /* Location parameters end */

        DiffManager diffManager = diffManagerCache.get(cid);
        if(diffManager == null) {
            diffManager = new DiffManager();
        }
        long id = cid == 0 ? System.currentTimeMillis() : cid;
        diffManagerCache.put(id, diffManager);

        setSSEHeader(response);

        try {
            for(int i = 0; i < 30; i++) {
                if(!asyncContext.getResponse().getWriter().checkError()) {
                    try {
                        callback.sseCallback(asyncContext.getResponse().getWriter(), diffManager, x, z);

                    if(!continous) {
                        asyncContext.getResponse().getWriter().write("\n\ndata: READY2CLOSE\n\n");
                        break;
                    }

                    Thread.sleep(intervalInMs);
                    } catch (InterruptedException ignored) {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if(continous) {
                asyncContext.getResponse().getWriter().write("\r\ndata: CID=" + id + "\r\n");
                asyncContext.getResponse().getWriter().write("\r\nretry: " + intervalInMs + "\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        asyncContext.complete();
    }

    private static void setSSEHeader(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Transfer-Encoding", "chunked");
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    protected static void sendStreamData(String data, PrintWriter writer) {
        writer.write("data: " + data + "\n\n");
        writer.flush();
    }

    protected static void sendStreamData(String eventName, String data, PrintWriter writer) {
        writer.write("event: " + eventName + "\n");
        writer.write("data: " + data + "\n\n");
        writer.flush();
    }

    protected static int getManhattanDistance(Vec3i vec1, Vec3i vec2) {
        float f = (float)Math.abs(vec1.getX() - vec2.getX());
        float g = (float)Math.abs(vec1.getY() - vec1.getY());
        float h = (float)Math.abs(vec1.getZ() - vec2.getZ());
        return (int)(f + g + h);
    }

    @FunctionalInterface
    public interface SSECallback {
        void sseCallback(PrintWriter writer, DiffManager diffManager, int x, int z);
    }
}