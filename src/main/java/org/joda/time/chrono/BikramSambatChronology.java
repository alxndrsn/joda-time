/*
 *  Copyright 2001-2017 authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.time.chrono;

import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeZone;
import org.joda.time.field.SkipDateTimeField;

/**
 * Implements the Bikram Sambat calendar system, which is primarily used in
 * Nepal.
 * <p>
 * Each year is dividded into 12 months of between 28 and 32 days, the lengths
 * of which are decided by a council of astrologers.
 * <p>
 * BikramSambatChronology is thread-safe and immutable.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Bikram_Sambat">Wikipedia</a>
 *
 * @author TODO
 * @since 1.? TODO
 */
public final class BikramSambatChronology extends BasicChronology {

    /** Serialization lock */
    private static final long serialVersionUID = -5972804258688333942L; // TODO

    /**
     * Constant value for 'Bikram Sambat Era', equivalent to the value returned
     * for AD/CE.
     */
    public static final int BS = DateTimeConstants.CE; // TODO 

    /** A singleton era field. */
    private static final DateTimeField ERA_FIELD = new BasicSingleEraDateTimeField("BS");

    /** The lowest year that can be fully supported. */
    private static final int MIN_YEAR = -292269337; // TODO

    /** The highest year that can be fully supported. */
    private static final int MAX_YEAR = 292272984; // TODO

    /** Cache of zone to chronology arrays */
    private static final ConcurrentHashMap<DateTimeZone, BikramSambatChronology[]> cCache = new ConcurrentHashMap<DateTimeZone, BikramSambatChronology[]>();

    /** Singleton instance of a UTC BikramSambatChronology */
    private static final BikramSambatChronology INSTANCE_UTC;
    static {
        // init after static fields
        INSTANCE_UTC = getInstance(DateTimeZone.UTC);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets an instance of the BikramSambatChronology.
     * The time zone of the returned instance is UTC.
     * 
     * @return a singleton UTC instance of the chronology
     */
    public static BikramSambatChronology getInstanceUTC() {
        return INSTANCE_UTC;
    }

    /**
     * Gets an instance of the BikramSambatChronology in the default time zone.
     * 
     * @return a chronology in the default time zone
     */
    public static BikramSambatChronology getInstance() {
        return getInstance(DateTimeZone.getDefault(), 4);
    }

    /**
     * Gets an instance of the BikramSambatChronology in the given time zone.
     * 
     * @param zone  the time zone to get the chronology in, null is default
     * @return a chronology in the specified time zone
     */
    public static BikramSambatChronology getInstance(DateTimeZone zone) {
        return getInstance(zone, 4);
    }

    /**
     * Gets an instance of the BikramSambatChronology in the given time zone.
     * 
     * @param zone  the time zone to get the chronology in, null is default
     * @param minDaysInFirstWeek  minimum number of days in first week of the year; default is 4
     * @return a chronology in the specified time zone
     */
    public static BikramSambatChronology getInstance(DateTimeZone zone, int minDaysInFirstWeek) { // TODO i don't think this first-day-of-week stuff is helpful to us, as we have variable-length months
        if (zone == null) {
            zone = DateTimeZone.getDefault();
        }
        BikramSambatChronology chrono;
        BikramSambatChronology[] chronos = cCache.get(zone);
        if (chronos == null) {
            chronos = new BikramSambatChronology[7];
            BikramSambatChronology[] oldChronos = cCache.putIfAbsent(zone, chronos);
            if (oldChronos != null) {
                chronos = oldChronos;
            }
        }
        try {
            chrono = chronos[minDaysInFirstWeek - 1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException
                ("Invalid min days in first week: " + minDaysInFirstWeek);
        }

        if (chrono == null) {
            synchronized (chronos) {
                chrono = chronos[minDaysInFirstWeek - 1];
                if (chrono == null) {
                    if (zone == DateTimeZone.UTC) {
                        // First create without a lower limit.
                        chrono = new BikramSambatChronology(null, null, minDaysInFirstWeek);
                        // Impose lower limit and make another BikramSambatChronology.
                        DateTime lowerLimit = new DateTime(1, 1, 1, 0, 0, 0, 0, chrono);
                        chrono = new BikramSambatChronology
                            (LimitChronology.getInstance(chrono, lowerLimit, null),
                             null, minDaysInFirstWeek);
                    } else {
                        chrono = getInstance(DateTimeZone.UTC, minDaysInFirstWeek);
                        chrono = new BikramSambatChronology
                            (ZonedChronology.getInstance(chrono, zone), null, minDaysInFirstWeek);
                    }
                    chronos[minDaysInFirstWeek - 1] = chrono;
                }
            }
        }
        return chrono;
    }

    // Constructors and instance variables
    //-----------------------------------------------------------------------
    /**
     * Restricted constructor.
     */
    BikramSambatChronology(Chronology base, Object param, int minDaysInFirstWeek) {
        super(base, param, minDaysInFirstWeek);
    }

    /**
     * Serialization singleton.
     */
    private Object readResolve() {
        Chronology base = getBase();
        return base == null ?
                getInstance(DateTimeZone.UTC, getMinimumDaysInFirstWeek()) :
                    getInstance(base.getZone(), getMinimumDaysInFirstWeek());
    }

    // Conversion
    //-----------------------------------------------------------------------
    /**
     * Gets the Chronology in the UTC time zone.
     * 
     * @return the chronology in UTC
     */
    public Chronology withUTC() {
        return INSTANCE_UTC;
    }

    /**
     * Gets the Chronology in a specific time zone.
     * 
     * @param zone  the zone to get the chronology in, null is default
     * @return the chronology
     */
    public Chronology withZone(DateTimeZone zone) {
        if (zone == null) {
            zone = DateTimeZone.getDefault();
        }
        if (zone == getZone()) {
            return this;
        }
        return getInstance(zone);
    }

    //-----------------------------------------------------------------------
    @Override
    boolean isLeapDay(long instant) { // TODO what does this even mean for BS?
        return dayOfMonth().get(instant) == 6 && monthOfYear().isLeap(instant);
    }

    //-----------------------------------------------------------------------
    long calculateFirstDayOfYearMillis(int year) {
        // Java epoch is 1970-01-01 Gregorian which is ????-??-?? BikramSambat. // TODO set this date proper
        // Calculate relative to the nearest leap year and account for the
        // difference later.

        // TODO whole content of this method is wrong - currently copied from Ethiopic calendar

        int relativeYear = year - 1963;
        int leapYears;
        if (relativeYear <= 0) {
            // Add 3 before shifting right since /4 and >>2 behave differently
            // on negative numbers.
            leapYears = (relativeYear + 3) >> 2;
        } else {
            leapYears = relativeYear >> 2;
            // For post 1963 an adjustment is needed as jan1st is before leap day
            if (!isLeapYear(year)) {
                leapYears++;
            }
        }
        
        long millis = (relativeYear * 365L + leapYears)
            * (long)DateTimeConstants.MILLIS_PER_DAY;

        // Adjust to account for difference between 1963-01-01 and 1962-04-23.

        return millis + (365L - 112) * DateTimeConstants.MILLIS_PER_DAY;
    }

    //-----------------------------------------------------------------------
    int getMinYear() {
        return MIN_YEAR;
    }

    //-----------------------------------------------------------------------
    int getMaxYear() {
        return MAX_YEAR;
    }

    //-----------------------------------------------------------------------
    long getApproxMillisAtEpochDividedByTwo() {
        return (1962L * MILLIS_PER_YEAR + 112L * DateTimeConstants.MILLIS_PER_DAY) / 2; // TODO change 1962 to something BS-appropriate
    }

    //-----------------------------------------------------------------------
    protected void assemble(Fields fields) {
        if (getBase() == null) {
            super.assemble(fields);

            // BikramSambat, like Julian, has no year zero.
            fields.year = new SkipDateTimeField(this, fields.year);
            fields.weekyear = new SkipDateTimeField(this, fields.weekyear);
            
            fields.era = ERA_FIELD;
            fields.monthOfYear = new BasicMonthOfYearDateTimeField(this, 13);
            fields.months = fields.monthOfYear.getDurationField();
        }
    }

}
