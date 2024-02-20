package org.example.client;

import org.example.response.Response;

import java.util.Random;

public class DefaultClient implements Client {

    @Override
    public Response getApplicationStatus1(String id) {
        this.sleep();

        return new Response.Success("getApplicationStatus1", id);
    }

    @Override
    public Response getApplicationStatus2(String id) {
        this.sleep();

        return new Response.Success("getApplicationStatus2", id);
    }

    private void sleep () {
        try {
            var sleep = new Random().nextInt(100, 300);
            Thread.sleep(sleep);
        } catch (InterruptedException ignore) {}
    }
}
