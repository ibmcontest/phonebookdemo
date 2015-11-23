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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("nls")
@RunWith(MockitoJUnitRunner.class)
public class PhonebookServiceHandlerTest {

    @Mock
    EntityManager em;
    @Mock
    UserTransaction utx;
    @Mock
    CriteriaBuilder builder;
    @Mock
    CriteriaQuery<PhonebookEntry> criteriaQuery;
    @Mock
    TypedQuery<PhonebookEntry> query;
    @Mock
    UriInfo uriInfo;
    @Mock
    UriBuilder uriBuilder;
    @Mock
    Root<PhonebookEntry> entryRoot;

    final long id = 1;
    final ArgumentCaptor<PhonebookEntry> argument = ArgumentCaptor.forClass(PhonebookEntry.class);

    @Test
    public void queryPhonebook() {

        final PhonebookEntry entry = new PhonebookEntry("Mr", "John", "Doe", "12345");
        when(em.getCriteriaBuilder()).thenReturn(builder);
        when(builder.createQuery(PhonebookEntry.class)).thenReturn(criteriaQuery);
        when(em.createQuery(criteriaQuery)).thenReturn(query);
        when(em.createQuery("SELECT t FROM PhonebookEntry t", PhonebookEntry.class)).thenReturn(query);
        when(criteriaQuery.select(Matchers.<Selection<? extends PhonebookEntry>> any()))
                .thenReturn(criteriaQuery);
        when(query.getResultList()).thenReturn(Arrays.asList(entry));

        final PhonebookServiceHandler phonebookServiceHandler = new PhonebookServiceHandler(utx, em, uriInfo);
        final PhonebookEntries entries = phonebookServiceHandler.queryPhonebook(null, null, null);

        assertEquals(1, entries.getEntries().size());
        assertEquals(true, entries.getEntries().get(0).equals(entry));

    }

    @Test
    public void getEntry() {
        final PhonebookEntry entry = new PhonebookEntry("Mr", "John", "Doe", "12345");
        when(em.find(PhonebookEntry.class, id)).thenReturn(entry);

        final PhonebookServiceHandler phonebookServiceHandler = new PhonebookServiceHandler(utx, em, uriInfo);
        final PhonebookEntry returnedEntry = phonebookServiceHandler.getEntry("1");

        assertEquals(true, returnedEntry.equals(entry));
    }

    @Test(expected = NotFoundException.class)
    public void getEntryNotFound() {
        when(em.find(PhonebookEntry.class, id)).thenReturn(null);

        final PhonebookServiceHandler phonebookServiceHandler = new PhonebookServiceHandler(utx, em, uriInfo);
        phonebookServiceHandler.getEntry("1");

    }

    @Test
    public void create() {
        try {
            when(uriInfo.getAbsolutePath()).thenReturn(new URI("http://localhost:1234/api/phonebook"));
        } catch (final URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        final PhonebookServiceHandler phonebookServiceHandler = new PhonebookServiceHandler(utx, em, uriInfo);
        final PhonebookEntry entry = new PhonebookEntry("Mr", "John", "Doe", "12345");
        entry.setId(id);
        final Response response = phonebookServiceHandler.create(entry);

        verify(em).persist(argument.capture());
        assertEquals(true, argument.getValue().equals(entry));
        assertEquals(201, response.getStatus());
        assertEquals("http://localhost:1234/api/phonebook/1", response.getLocation().toString());
    }

    @Test
    public void update() {
        when(em.find(PhonebookEntry.class, id)).thenReturn(new PhonebookEntry("Mr", "John", "Doe", "12345"));

        final PhonebookServiceHandler phonebookServiceHandler = new PhonebookServiceHandler(utx, em, uriInfo);
        final PhonebookEntry entry = new PhonebookEntry("Mr", "Jack", "Doe", "12345");
        final Response response = phonebookServiceHandler.update("1", entry);

        verify(em).merge(argument.capture());
        assertEquals(true, argument.getValue().equals(entry));
        assertEquals(204, response.getStatus());
    }

    @Test(expected = NotFoundException.class)
    public void updateNotFound() {
        when(em.find(PhonebookEntry.class, id)).thenReturn(null);

        final PhonebookServiceHandler phonebookServiceHandler = new PhonebookServiceHandler(utx, em, uriInfo);
        final PhonebookEntry entry = new PhonebookEntry("Mr", "Jack", "Doe", "12345");
        phonebookServiceHandler.update("1", entry);
    }

    @Test
    public void deleteEntry() {
        final PhonebookEntry entry = new PhonebookEntry("Mr", "John", "Doe", "12345");
        when(em.find(PhonebookEntry.class, id)).thenReturn(entry);

        final PhonebookServiceHandler phonebookServiceHandler = new PhonebookServiceHandler(utx, em, uriInfo);
        final Response response = phonebookServiceHandler.deleteEntry("1");

        verify(em).remove(argument.capture());
        assertEquals(true, argument.getValue().equals(entry));
        assertEquals(204, response.getStatus());
    }

    @Test(expected = NotFoundException.class)
    public void deleteEntryNotFound() {
        when(em.find(PhonebookEntry.class, id)).thenReturn(null);

        final PhonebookServiceHandler phonebookServiceHandler = new PhonebookServiceHandler(utx, em, uriInfo);
        phonebookServiceHandler.deleteEntry("1");

    }

}
