package org.iiitb.EC.rest_services;

import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.iiitb.EC.dao.DAO_BankAccount;
import org.iiitb.EC.dao.DAO_Buyer;
import org.iiitb.EC.dao.DAO_Order_Details;
import org.iiitb.EC.dao.DAO_Seller;
import org.iiitb.EC.model.Order_Details;

@Path("bankService")
public class BankAccountService {
	
	private static final String SUCCESS_RESULT="Success";
	private static final String FAILURE_RESULT="Failure";
	
	@Path("getBuyerBalance")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String GetBuyerBalance(@Context HttpHeaders httpheaders) {
		String username = httpheaders.getHeaderString("username");
		int buyer_id = DAO_Buyer.get_buyer_id(username);
		
		int balance = DAO_BankAccount.getBuyerAccountBalance(buyer_id);
		return String.valueOf(balance);
	}
	
	@Path("getSellerBalance")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String GetSellerBalance(@Context HttpHeaders httpheaders) {
		String username = httpheaders.getHeaderString("username");
		int seller_id = DAO_Seller.get_seller_id(username);
		
		int balance = DAO_BankAccount.getSellerAccountBalance(seller_id);
		System.out.println(seller_id);
		return String.valueOf(balance);
	}
	
	@Path("getEbayBalance")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String GetEbayBalance(@Context HttpHeaders httpheaders) {
		int balance = DAO_BankAccount.getEbayAccountBalance();
		return String.valueOf(balance);
	}
	
	@Path("addBuyerWallet")
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public String AddBuyerWallet(String data, @Context HttpHeaders httpheaders) {
		int balanceToAdd = Integer.parseInt(data);
		int buyer_id = DAO_Buyer.get_buyer_id(httpheaders.getHeaderString("username"));
		int newBalance = DAO_BankAccount.getBuyerAccountBalance(buyer_id) + balanceToAdd;
		boolean result = DAO_BankAccount.setBuyerAccountBalance(buyer_id, newBalance);
		
		
		return result ? SUCCESS_RESULT : FAILURE_RESULT;
	}

	@Path("transaction")
	@POST
    @Produces(MediaType.TEXT_PLAIN)
	public String PerformTransaction(String data, @Context HttpHeaders httpheaders) {
		String accountNumber = data;
		long order_id = DAO_Order_Details.get_last_order_id();
		ArrayList<Order_Details> orders = DAO_Order_Details.getOrder((int)order_id);
		int buyer_id = DAO_Buyer.get_buyer_id(httpheaders.getHeaderString("username"));
		boolean isValidAccount = DAO_BankAccount.checkAccountValidity(buyer_id, accountNumber);
		boolean result = true;
		
		System.out.println("Acc: " + data);
		System.out.println("UN: " + buyer_id);
		if (isValidAccount) {
			for (int i = 0; i < orders.size(); i++) {
				Order_Details order = orders.get(i);
				int seller_id = order.getSeller_id();
				int item_id = order.getItem_id();
				
				int transactionAmount = order.getTotal_amount();
							
				result = DAO_BankAccount.performTransaction(transactionAmount, (int)order_id, item_id, buyer_id, seller_id);
				if (!result) {
					return FAILURE_RESULT;
				}
			}
		}
		return result ? SUCCESS_RESULT : FAILURE_RESULT;
	}
}
