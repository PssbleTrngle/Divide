package com.possible_triangle.divide.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.possible_triangle.divide.network.CacheRequest;
import com.possible_triangle.divide.network.Overview;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Server {

    private static final HashMap<MinecraftServer,Server> RUNNING = Maps.newHashMap();

    @SubscribeEvent
    public static void serverStart(FMLServerStartingEvent event) {
        Server server = new Server(event.getServer());
        try {
            server.start();
            RUNNING.put(event.getServer(), server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void serverStopped(FMLServerStoppingEvent event) {
        Optional.ofNullable(RUNNING.get(event.getServer())).ifPresent(Server::stop);
    }

    private static final int PORT = 8080;
    private final ServerWorld world;
    private HttpServer server;

    public Server(MinecraftServer server) {
        this.world = server.getWorld(DimensionType.OVERWORLD);
    }

    public void start() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/overview", this::sendOverview);
        server.createContext("/gametime", this::sendGametime);
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private void sendJSON(HttpExchange res, Object object) throws IOException {
        Gson gson = new Gson();
        sendJSON(res, gson.toJson(object));
    }

    private void sendJSON(HttpExchange res, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        res.sendResponseHeaders(200, bytes.length);
        res.getResponseHeaders().set("Content-Type", "application/json");
        try {
            res.getResponseBody().write(bytes);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            res.close();
        }
    }

    private void sendGametime(HttpExchange res) throws IOException {
        long time = world.getGameTime();
        sendJSON(res, "{\"time\":" + time + "}");
    }

    private void sendOverview(HttpExchange res) throws IOException {
        Overview overview = CacheRequest.create(world, null);
        sendJSON(res, overview);
    }

}
