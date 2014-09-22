package com.dlohaiti.dlokiosk.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import com.dlohaiti.dlokiosk.db.KioskDatabase.SponsorsTable;
import com.dlohaiti.dlokiosk.domain.Sponsor;
import com.dlohaiti.dlokiosk.domain.Sponsors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SponsorRepositoryTest {

    KioskDatabase db = new KioskDatabase(Robolectric.application.getApplicationContext());
    SponsorRepository repository;

    @Before
    public void setUp() {
        repository = new SponsorRepository(db);
    }

    @Test
    public void shouldReturnEmptySetWhenNoSponsors() {
        Sponsors list = repository.findAll();
        assertThat(list.size(), is(0));
    }

    @Test
    public void shouldReturnAllSponsorsInSetInAlphabeticalOrder() {
        SQLiteDatabase wdb = db.getWritableDatabase();
        List<Sponsor> sponsors = asList(new Sponsor(1L, "Name 1", "Contact Name 1", "Desc 1"),
                new Sponsor(2L, "Name 2", "Contact Name 2", "Desc 2"));
        saveSponsors(wdb, sponsors);

        Sponsors list = repository.findAll();

        assertThat(list.size(), is(2));
        assertThat(list, is(asList(new Sponsor(1L, "Name 1", "Contact Name 1", "Desc 1"),
                new Sponsor(2L, "Name 2", "Contact Name 2", "Desc 2"))));
    }

    @Test
    public void shouldReplaceAll() {
        SQLiteDatabase wdb = db.getWritableDatabase();
        saveSponsors(wdb, asList(new Sponsor(1L, "Name 1", "Contact Name 1", "Desc 1"),
                new Sponsor(2L, "Name 2", "Contact Name 2", "Desc 2")));
        assertThat(repository.findAll(),
                is(asList(new Sponsor(1L, "Name 1", "Contact Name 1", "Desc 1"),
                        new Sponsor(2L, "Name 2", "Contact Name 2", "Desc 2"))));

        boolean success = repository.replaceAll(asList(new Sponsor(3L, "Name 3", "Contact Name 3", "Desc 3"),
                new Sponsor(4L, "Name 4", "Contact Name 4", "Desc 3")));

        assertThat(success, is(true));
        assertThat(repository.findAll(),
                is(asList(new Sponsor(3L, "Name 3", "Contact Name 3", "Desc 3"),
                        new Sponsor(4L, "Name 4", "Contact Name 4", "Desc 3"))));
    }

    private void saveSponsors(SQLiteDatabase wdb, List<Sponsor> sponsors) {
        for (Sponsor sponsor : sponsors) {
            ContentValues values = new ContentValues();
            values.put(SponsorsTable.ID, sponsor.id());
            values.put(SponsorsTable.NAME, sponsor.name());
            values.put(SponsorsTable.CONTACT_NAME, sponsor.contactName());
            values.put(SponsorsTable.DESCRIPTION, sponsor.description());
            wdb.insert(SponsorsTable.TABLE_NAME, null, values);
        }
    }
}