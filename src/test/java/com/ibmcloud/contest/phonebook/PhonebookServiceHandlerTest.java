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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import com.ibmcloud.contest.phonebook.util.CustomUserTransaction;

@SuppressWarnings("nls")
@RunWith(MockitoJUnitRunner.class)
public class PhonebookServiceHandlerTest {

    private static final String DUMMYHOST = "http://dummyhost:1234/api/phonebook";
    private static final String USERKEY = "12345678900";
    private static EntityManager em;
    private static UserTransaction utx;

    @Mock
    private UriInfo uriInfo;

    private PhonebookServiceHandler phonebookServiceHandler;
    private UserServiceHandler userServiceHandler;

    @BeforeClass
    public static void initialize() throws Exception {
        em = Persistence.createEntityManagerFactory("phonebook-junit").createEntityManager();
        utx = new CustomUserTransaction(em);
    }

    @AfterClass
    public static void terminate() {
        em.close();
    }

    @Before
    public void beforeEach() throws Exception {
        utx.begin();
        em.createQuery("DELETE FROM PhonebookEntry").executeUpdate();
        em.createQuery("DELETE FROM UserEntry").executeUpdate();
        utx.commit();

        // Mock out uriInfo, it's easier then implementing it, but need to know which method is being called.
        when(uriInfo.getAbsolutePath()).thenReturn(new URI(DUMMYHOST));

        phonebookServiceHandler = new PhonebookServiceHandler(utx, em, uriInfo);
        userServiceHandler = new UserServiceHandler(utx, em, uriInfo);
    }

    private void createEntries(final List<PhonebookEntry> entries) throws Exception {
        utx.begin();
        for (final PhonebookEntry entry : entries) {
            em.persist(entry);
        }
        em.flush();
        utx.commit();
    }

    private void addUser(final UserEntry user) throws Exception {
        utx.begin();
        em.persist(user);
        em.flush();
        utx.commit();
    }

    @Test
    public void createUser() {
        final UserEntry entry = (UserEntry) userServiceHandler.createUser().getEntity();
        assertEquals(entry.getKey().length(), 11);

        final UserEntry findEntry = em.find(UserEntry.class, entry.getKey());
        assertEquals(findEntry.getKey(), entry.getKey());

    }

    @Test
    public void initPhonebook() throws Exception {
        when(uriInfo.getAbsolutePath()).thenReturn(new URI(DUMMYHOST));

        addUser(new UserEntry(USERKEY));
        final PhonebookEntries entries = phonebookServiceHandler.queryPhonebook(USERKEY, null, null, null);

        assertEquals(2, entries.getEntries().size());
        final PhonebookEntry default1 = new PhonebookEntry("Mr", "Fred", "Jones", "01962 000000");
        final PhonebookEntry default2 = new PhonebookEntry("Mrs", "Jane", "Doe", "01962 000001");

        assertTrue(entries.getEntries().get(0).equals(default1));
        assertTrue(entries.getEntries().get(1).equals(default2));
    }

    @Test
    public void queryPhonebook() throws Exception {
        addUser(new UserEntry(USERKEY));
        final PhonebookEntry entry1 = new PhonebookEntry("Mr", "John", "Smith", "12345", USERKEY);
        final PhonebookEntry entry2 = new PhonebookEntry("Ms", "Jane", "Doe", "67890", USERKEY);
        final PhonebookEntry entry3 = new PhonebookEntry("Ms", "Jessica", "Rabbit", "1111-2222", USERKEY);
        createEntries(Arrays.asList(entry1, entry2, entry3));

        PhonebookEntries entries = phonebookServiceHandler.queryPhonebook(USERKEY, null, null, null);
        assertEquals(3, entries.getEntries().size());

        entries = phonebookServiceHandler.queryPhonebook(USERKEY, "Ms", null, null);
        assertEquals(2, entries.getEntries().size());

        entries = phonebookServiceHandler.queryPhonebook(USERKEY, "Ms", "Jane", null);
        assertEquals(1, entries.getEntries().size());
        assertEquals(true, entries.getEntries().get(0).equals(entry2));

        entries = phonebookServiceHandler.queryPhonebook(USERKEY, null, "John", null);
        assertEquals(1, entries.getEntries().size());
        assertEquals(true, entries.getEntries().get(0).equals(entry1));

        entries = phonebookServiceHandler.queryPhonebook(USERKEY, null, null, "Rabbit");
        assertEquals(1, entries.getEntries().size());
        assertEquals(true, entries.getEntries().get(0).equals(entry3));
    }

    @Test(expected = UnauthorizedException.class)
    public void queryPhonebookUnauthorized() throws Exception {
        phonebookServiceHandler.queryPhonebook(USERKEY, null, null, null);
    }

    @Test
    public void getEntry() throws Exception {

        addUser(new UserEntry(USERKEY));

        final PhonebookEntry entry1 = new PhonebookEntry("Mr", "John", "Smith", "12345", USERKEY);
        final PhonebookEntry entry2 = new PhonebookEntry("Mrs", "Jane", "Doe", "67890", USERKEY);
        final PhonebookEntry entry3 = new PhonebookEntry("Ms", "Jessica", "Rabbit", "1111-2222", USERKEY);
        createEntries(Arrays.asList(entry1, entry2, entry3));

        PhonebookEntry returnedEntry = phonebookServiceHandler.getEntry(USERKEY,
                String.valueOf(entry1.getId()));
        assertEquals(true, returnedEntry.equals(entry1));

        returnedEntry = phonebookServiceHandler.getEntry(USERKEY, String.valueOf(entry2.getId()));
        assertEquals(true, returnedEntry.equals(entry2));

        returnedEntry = phonebookServiceHandler.getEntry(USERKEY, String.valueOf(entry3.getId()));
        assertEquals(true, returnedEntry.equals(entry3));
    }

    @Test(expected = NotFoundException.class)
    public void getEntryNotFound() throws Exception {
        addUser(new UserEntry(USERKEY));
        final PhonebookEntry entry1 = new PhonebookEntry("Mr", "John", "Smith", "12345");
        final PhonebookEntry entry2 = new PhonebookEntry("Mrs", "Jane", "Doe", "67890");
        final PhonebookEntry entry3 = new PhonebookEntry("Ms", "Jessica", "Rabbit", "1111-2222");
        createEntries(Arrays.asList(entry1, entry2, entry3));

        phonebookServiceHandler.getEntry(USERKEY, "10000");
    }

    @Test(expected = NotFoundException.class)
    public void getEntryBadId() throws Exception {
        addUser(new UserEntry(USERKEY));
        phonebookServiceHandler.getEntry(USERKEY, "blah");
    }

    @Test(expected = UnauthorizedException.class)
    public void getEntryUnauthorized() throws Exception {
        phonebookServiceHandler.getEntry(USERKEY, "10000");
    }

    @Test
    public void create() throws Exception {

        addUser(new UserEntry(USERKEY));

        final PhonebookEntry entry = new PhonebookEntry("Mr", "John", "Doe", "12345");
        Response response = phonebookServiceHandler.create(USERKEY, entry);

        assertEquals(201, response.getStatus());
        String expectedLocation = DUMMYHOST + "/" + entry.getId();
        assertEquals(expectedLocation, response.getLocation().toString());

        // Create a second one
        final PhonebookEntry entry2 = new PhonebookEntry("Mrs", "Jane", "Doe", "67890");
        response = phonebookServiceHandler.create(USERKEY, entry2);

        assertEquals(201, response.getStatus());
        expectedLocation = DUMMYHOST + "/" + entry2.getId();
        assertEquals(expectedLocation, response.getLocation().toString());

        assertFalse(entry.getId() == entry2.getId());

    }

    @Test(expected = UnauthorizedException.class)
    public void createUnauthorized() throws Exception {
        phonebookServiceHandler.create(USERKEY, new PhonebookEntry("Mr", "John", "Doe", "12345"));
    }

    @Test
    public void update() throws Exception {
        addUser(new UserEntry(USERKEY));
        final PhonebookEntry entry = new PhonebookEntry("Mr", "John", "Doe", "12345", USERKEY);
        createEntries(Arrays.asList(entry));

        final PhonebookEntry updateEntry = new PhonebookEntry("Mr", "Jack", "Doe", "12345", USERKEY);
        final Response response = phonebookServiceHandler.update(USERKEY, String.valueOf(entry.getId()),
                updateEntry);

        assertEquals(204, response.getStatus());

        final PhonebookEntry returnedEntry = phonebookServiceHandler.getEntry(USERKEY,
                String.valueOf(entry.getId()));
        assertEquals(true, returnedEntry.equals(updateEntry));
    }

    @Test(expected = NotFoundException.class)
    public void updateNotFound() throws Exception {
        addUser(new UserEntry(USERKEY));
        final PhonebookEntry updateEntry = new PhonebookEntry("Mr", "Jack", "Doe", "12345");
        phonebookServiceHandler.update(USERKEY, "10000", updateEntry);
    }

    @Test(expected = NotFoundException.class)
    public void updateBadId() throws Exception {
        addUser(new UserEntry(USERKEY));
        phonebookServiceHandler.update(USERKEY, "blah", new PhonebookEntry("Mr", "Jack", "Doe", "12345"));
    }

    @Test(expected = UnauthorizedException.class)
    public void updateUnauthorized() throws Exception {
        phonebookServiceHandler.update(USERKEY, "10000",
                new PhonebookEntry("Mr", "Jack", "Doe", "12345", "jdoe@email.com"));
    }

    @Test
    public void deleteEntry() throws Exception {
        addUser(new UserEntry(USERKEY));
        final PhonebookEntry entry1 = new PhonebookEntry("Mr", "John", "Smith", "12345", USERKEY);
        final PhonebookEntry entry2 = new PhonebookEntry("Mrs", "Jane", "Doe", "67890", USERKEY);
        final PhonebookEntry entry3 = new PhonebookEntry("Ms", "Jessica", "Rabbit", "1111-2222", USERKEY);
        createEntries(Arrays.asList(entry1, entry2, entry3));

        Response response = phonebookServiceHandler.deleteEntry(USERKEY, String.valueOf(entry2.getId()));
        assertEquals(204, response.getStatus());
        assertEquals(2,
                phonebookServiceHandler.queryPhonebook(USERKEY, null, null, null).getEntries().size());

        response = phonebookServiceHandler.deleteEntry(USERKEY, String.valueOf(entry1.getId()));
        assertEquals(204, response.getStatus());
        final PhonebookEntries entries = phonebookServiceHandler.queryPhonebook(USERKEY, null, null, null);
        assertEquals(1, entries.getEntries().size());
        assertEquals(true, entries.getEntries().get(0).equals(entry3));

        response = phonebookServiceHandler.deleteEntry(USERKEY, String.valueOf(entry3.getId()));
        assertEquals(204, response.getStatus());
    }

    @Test(expected = NotFoundException.class)
    public void deleteEntryNotFound() throws Exception {
        addUser(new UserEntry(USERKEY));
        phonebookServiceHandler.deleteEntry(USERKEY, "10000");
    }

    @Test(expected = NotFoundException.class)
    public void deleteEntryBadId() throws Exception {
        addUser(new UserEntry(USERKEY));
        phonebookServiceHandler.deleteEntry(USERKEY, "blah");
    }

    @Test(expected = UnauthorizedException.class)
    public void deleteEntryUnauthorized() throws Exception {
        phonebookServiceHandler.deleteEntry(USERKEY, "10000");
    }

}
