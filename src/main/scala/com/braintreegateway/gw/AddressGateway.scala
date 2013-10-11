package com.braintreegateway.gw

import com.braintreegateway.exceptions.NotFoundException
import com.braintreegateway.util.Http
import com.braintreegateway.{Address, AddressRequest}

/**
 * Provides methods to create, delete, find, and update {@link Address} objects.
 * This class does not need to be instantiated directly.
 * Instead, use {@link BraintreeGateway#address()} to get an instance of this class:
 *
 * <pre>
 * BraintreeGateway gateway = new BraintreeGateway(...);
 * gateway.address().create(...)
 * </pre>
 */
class AddressGateway(http: Http) {

  /**
   * Creates an {@link Address} for a {@link Customer}.
   * @param customerId the id of the { @link Customer}.
   * @param request the request object.
   * @return a { @link Result} object.
   */
  def create(customerId: String, request: AddressRequest): Result[Address] = {
    val node = http.post("/customers/" + customerId + "/addresses", request)
    Result.address(node)
  }

  /**
   * Deletes a Customer's {@link Address}.
   * @param customerId the id of the { @link Customer}.
   * @param id the id of the { @link Address} to delete.
   * @return a { @link Result} object.
   */
  def delete(customerId: String, id: String): Result[Address] = {
    http.delete("/customers/" + customerId + "/addresses/" + id)
    Result.deleted
  }

  /**
   * Finds a Customer's {@link Address}.
   * @param customerId the id of the { @link Customer}.
   * @param id the id of the { @link Address}.
   * @return the { @link Address} or raises a { @link com.braintreegateway.exceptions.NotFoundException}.
   */
  def find(customerId: String, id: String): Address = {
    if (customerId == null || (customerId.trim == "") || id == null || (id.trim == "")) throw new NotFoundException
    new Address(http.get("/customers/" + customerId + "/addresses/" + id))
  }

  /**
   * Updates a Customer's {@link Address}.
   * @param customerId the id of the { @link Customer}.
   * @param id the id of the { @link Address}.
   * @param request the request object containing the { @link AddressRequest} parameters.
   * @return the { @link Address} or raises a { @link com.braintreegateway.exceptions.NotFoundException}.
   */
  def update(customerId: String, id: String, request: AddressRequest): Result[Address] = {
    val node = http.put("/customers/" + customerId + "/addresses/" + id, request)
    Result.address(node)
  }
}
