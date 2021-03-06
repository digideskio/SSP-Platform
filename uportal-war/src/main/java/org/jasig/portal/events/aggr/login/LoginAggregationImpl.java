/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.events.aggr.login;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalIdCache;
import org.jasig.portal.events.aggr.AggregationInterval;
import org.jasig.portal.events.aggr.BaseAggregationImpl;
import org.jasig.portal.events.aggr.DateDimension;
import org.jasig.portal.events.aggr.TimeDimension;
import org.jasig.portal.events.aggr.UniqueStrings;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UP_LOGIN_EVENT_AGGR")
@Inheritance(strategy=InheritanceType.JOINED)
@SequenceGenerator(
        name="UP_LOGIN_EVENT_AGGR_GEN",
        sequenceName="UP_LOGIN_EVENT_AGGR_SEQ",
        allocationSize=1000
    )
@TableGenerator(
        name="UP_LOGIN_EVENT_AGGR_GEN",
        pkColumnValue="UP_LOGIN_EVENT_AGGR_PROP",
        allocationSize=1000
    )
@org.hibernate.annotations.Table(
        appliesTo = "UP_LOGIN_EVENT_AGGR", 
        indexes = @Index(name = "IDX_UP_LOGIN_EVENT_AGGR_DTI", columnNames = { "DATE_DIMENSION_ID", "TIME_DIMENSION_ID", "AGGR_INTERVAL" }))
@NaturalIdCache
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public final class LoginAggregationImpl 
        extends BaseAggregationImpl<LoginAggregationKey> 
        implements LoginAggregation, Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(generator = "UP_LOGIN_EVENT_AGGR_GEN")
    @Column(name="ID")
    private final long id;
    
    @Column(name = "LOGIN_COUNT", nullable = false)
    private int loginCount;
    
    @Column(name = "UNIQUE_LOGIN_COUNT", nullable = false)
    private int uniqueLoginCount;

    @OneToOne(cascade = { CascadeType.ALL }, orphanRemoval=true)
    @JoinColumn(name = "UNIQUE_STRINGS_ID")
    @Fetch(FetchMode.JOIN)
    private UniqueStrings uniqueStrings;

    @Transient
    private LoginAggregationKeyImpl aggregationKey;
    
    @SuppressWarnings("unused")
    private LoginAggregationImpl() {
        this.id = -1;
    }
    
    LoginAggregationImpl(TimeDimension timeDimension, DateDimension dateDimension, 
            AggregationInterval interval, AggregatedGroupMapping aggregatedGroup) {
        super(timeDimension, dateDimension, interval, aggregatedGroup);
        this.id = -1;
    }
    
    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public int getLoginCount() {
        return this.loginCount;
    }

    @Override
    public int getUniqueLoginCount() {
        return this.uniqueLoginCount;
    }
    
    @Override
    public LoginAggregationKey getAggregationKey() {
        LoginAggregationKeyImpl key = this.aggregationKey;
        if (key == null) {
            key = new LoginAggregationKeyImpl(this);
            this.aggregationKey = key;
        }
        return key;
    }

    @Override
    protected boolean isComplete() {
        return this.loginCount > 0 && this.uniqueStrings == null;
    }

    @Override
    protected void completeInterval() {
        this.uniqueStrings = null;
    }

    void countUser(String userName) {
        checkState();
        
        if (this.uniqueStrings == null) {
            this.uniqueStrings = new UniqueStrings();
        }
        
        if (this.uniqueStrings.add(userName)) {
            this.uniqueLoginCount++;
        }
        this.loginCount++;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof LoginAggregation))
            return false;
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "LoginAggregationImpl [id=" + id + ", timeDimension=" + getTimeDimension() + ", dateDimension=" + getDateDimension()
                + ", interval=" + getInterval() + ", groupName=" + getAggregatedGroup() + ", duration=" + getDuration() + ", loginCount="
                + loginCount + ", uniqueLoginCount=" + uniqueLoginCount + "]";
    }
}
