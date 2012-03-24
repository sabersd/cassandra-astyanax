/*
 * Copyright 2012 ReachLocal Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.reachlocal.grails.plugins.cassandra.test

import com.netflix.astyanax.model.ColumnFamily
import com.netflix.astyanax.serializers.StringSerializer

/**
 * @author: Bob Florian
 */
class AstyanaxServiceTests extends GroovyTestCase
{
	def astyanaxService

	void testRoundTrip()
	{
		astyanaxService.withKeyspace() {keyspace ->
			def cf = new ColumnFamily("User", StringSerializer.get(), StringSerializer.get())
			def m = keyspace.prepareMutationBatch()
			def cols = [id: UUID.randomUUID().toString(), email: "jdoe@localhost.com", name: "John Doe", city: "Olney", state: "MD"]
			m.withRow(cf, cols.id).putColumns(cols)
			m.execute()

			def u = keyspace.prepareQuery(cf).getKey(cols.id).execute().result
			println u.name.stringValue
			
			assertEquals(cols.id, u.id.stringValue)
			assertEquals(cols.name, u.name.stringValue)
			assertEquals(cols.email, u.email.stringValue)
			assertEquals(cols.city, u.city.stringValue)
			assertEquals(cols.state, u.state.stringValue)
		}
	}

	void testCql()
	{
		def count = 0
		def cql = astyanaxService.cql();
		cql.eachRow("select * from User limit 10") {row ->
			//println "${row.name}"
			count++
		}
		
		assertTrue count <= 10
	}

	void testShowColumnFamilies()
	{
		astyanaxService.showColumnFamilies(["User"])
		astyanaxService.showColumnFamilies(["User"], astyanaxService.defaultKeyspace, 10, 3, System.err)
	}

}
