/**
 * Copyright 2015 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibmcloud.contest.phonebook;

import java.util.Random;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;

@Path("/user")
@Api(value = "/user")
/**
 *
 */

public class UserServiceHandler {

    @Context
    UriInfo uriInfo;

    private final UserTransaction utx;
    private final EntityManager em;

    public UserServiceHandler() {
        utx = getUserTransaction();
        em = getEm();
    }

    public UserServiceHandler(final UserTransaction utx, final EntityManager em, final UriInfo uriInfo) {
        this.utx = utx;
        this.em = em;
        this.uriInfo = uriInfo;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates new user entry", response = UserEntry.class)
    @ApiResponse(code = 201, message = "User created successfully")
    public Response createUser() {

        final String key = generateKey();
        final UserEntry user = new UserEntry(key);
        try {
            utx.begin();
            em.persist(user);
            utx.commit();
            return Response.status(201).entity(user).build();
        } catch (final Exception e) {
            e.printStackTrace();
            throw new WebApplicationException();
        } finally {
            try {
                if (utx.getStatus() == Status.STATUS_ACTIVE) {
                    utx.rollback();
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

    }

    private String generateKey() {
        final String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"; //$NON-NLS-1$
        final Random rand = new Random();
        final int length = 11;
        final StringBuilder builder = new StringBuilder(length);
        String key;
        UserEntry checkEntry;
        do {
            for (int i = 0; i < length; i++) {
                builder.append(characters.charAt(rand.nextInt(characters.length())));
            }
            key = builder.toString();
            checkEntry = em.find(UserEntry.class, key);
        } while (checkEntry != null);

        return key;
    }

    private UserTransaction getUserTransaction() {
        InitialContext ic;
        try {
            ic = new InitialContext();
            return (UserTransaction) ic.lookup("java:comp/UserTransaction"); //$NON-NLS-1$
        } catch (final NamingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private EntityManager getEm() {
        InitialContext ic;
        try {
            ic = new InitialContext();
            return (EntityManager) ic.lookup("java:comp/env/openjpa-phonebook/entitymanager"); //$NON-NLS-1$
        } catch (final NamingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
