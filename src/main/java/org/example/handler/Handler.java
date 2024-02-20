package org.example.handler;

import org.example.response.ApplicationStatusResponse;

public interface Handler {

    ApplicationStatusResponse performOperation(String id);

}