/*
 * Copyright 1999-2015 Oliver Rode http://www.serviceflow.de/nutshell
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.serviceflow.nutshell.cl.test1;

/**
 * Some math to test if the communication is working.
 * 
 * @author Rodeo <a href="https://java.net/people/1022463-Rodeo">@
 *         java.net</a>
 * @version $Id: $
 * 
 * 
 */
public class MuliplicationTest {
	public final int factor1;
	public final int factor2;
	private final int clientCalculatedProduct;
	private int serverCalculatedProduct = -1;

	public MuliplicationTest(int factor1, int factor2) {
		this.factor1 = factor1;
		this.factor2 = factor2;
		clientCalculatedProduct = factor1 * factor2;
	}

	public int getClientCalculatedProduct() {
		return clientCalculatedProduct;
	}

	public void setServerCalculatedProduct(int product) {
		this.serverCalculatedProduct = product;
	}

	public int getServerCalculatedProduct() {
		return serverCalculatedProduct;
	}
}