package com.business;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.data.Item;
import com.data.ItemRepository;
import com.data.Notification;
import com.data.NotificationRepository;
import com.data.ReportFactory;
import com.presentation.DonateFoodWindow;
import com.presentation.MainWindow;
import com.presentation.NewGroceryItemWindow;


public class Utils {
	private MainWindow mw;
	private List <String> messages = new ArrayList <String> ();
	private List <String> names = new ArrayList <String> ();
	private List <Integer> quantities = new ArrayList <Integer> ();
	private List <Integer> calories = new ArrayList <Integer> ();
	private List <Date> purchaseDates = new ArrayList <Date> ();
	private List <Date> expirationDates = new ArrayList <Date> ();
	private List <Item> itemsToRemove = new ArrayList <Item> ();
	
	private ItemRepository itemRep;
	private NotificationRepository notRep;
	private int calorieGoal = 2000;
	
	public Utils(MainWindow mw, ItemRepository itemRep, NotificationRepository notRep) {
		this.mw = mw;
		this.itemRep = itemRep;
		this.notRep = notRep;
		mw.nglw.btnFinish.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				names = mw.nglw.getNames();
				quantities = mw.nglw.getQuantities();
				calories = mw.nglw.getCalories();
				purchaseDates = mw.nglw.getPurchaseDates();
				expirationDates = mw.nglw.getExpirationDates();
				
				Iterator <String> namesIterator = names.iterator();
				Iterator <Integer> quantitiesIterator = quantities.iterator();
				Iterator <Integer> caloriesIterator = calories.iterator();
				Iterator <Date> purchaseDatesIterator = purchaseDates.iterator();
				Iterator <Date> expirationDatesIterator = expirationDates.iterator();
				
				while(namesIterator.hasNext()) {
					Item newItem = new Item();
					newItem.setName(namesIterator.next());
					newItem.setQuantity(quantitiesIterator.next());
					newItem.setCalories(caloriesIterator.next());
					newItem.setPurchase(convertUtilToSql(purchaseDatesIterator.next()));
					newItem.setExpiration(convertUtilToSql(expirationDatesIterator.next()));
					
					itemRep.save(newItem);
				}
			}
		});
		mw.btnViewGroceries.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				List <Item> items = Utils.this.getItems();
				names = new ArrayList <String> ();
				quantities = new ArrayList <Integer> ();
				calories = new ArrayList <Integer> ();
				purchaseDates = new ArrayList <Date> ();
				expirationDates = new ArrayList <Date> ();
				
				Iterator <Item> it = items.iterator();
				
				while(it.hasNext()) {
					Item currentItem = it.next();
					names.add(currentItem.getName());
					quantities.add(currentItem.getQuantity());
					calories.add(currentItem.getCalories());
					purchaseDates.add(currentItem.getPurchase());
					expirationDates.add(currentItem.getExpiration());
				}
				
				mw.vgw.setNames(names);
				mw.vgw.setQuantities(quantities);
				mw.vgw.setCalories(calories);
				mw.vgw.setPurchaseDates(purchaseDates);
				mw.vgw.setExpirationDates(expirationDates);
				
				mw.vgw.display();
			}
		});
		
		mw.btnViewNotifications.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Utils.this.checkStatus();
				List <Notification> notifications = Utils.this.getNotifications();
				messages = new ArrayList <String> ();
				
				Iterator <Notification> it = notifications.iterator();
				
				while(it.hasNext()) {
					Notification currentNotification = it.next();
					messages.add(currentNotification.getMessage());
				}
				
				mw.vnw.setMessages(messages);
				mw.vnw.display();
			}
		});
		
		mw.btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int newGoal = mw.getNewCalorieGoal();
				if(newGoal != -1)
					Utils.this.calorieGoal = newGoal;
			}
		});
		
		mw.btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				List <String> options = Utils.this.getNamesAndQuantities();
				List <Item> currentItemsState = Utils.this.getItems();
				itemsToRemove = new ArrayList <Item> ();
				mw.dfw = new DonateFoodWindow(options);
				mw.dfw.setVisible(true);
				mw.dfw.addWindowListener(new WindowAdapter(){
	                public void windowClosing(WindowEvent e){
	                	
	                	}
				});
				mw.dfw.btnNewButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if(mw.dfw.getSelectedIndex() != -1) {
							Item selectedItem = currentItemsState.get(mw.dfw.getSelectedIndex());
							options.remove(mw.dfw.getSelectedIndex());
							currentItemsState.remove(mw.dfw.getSelectedIndex());
							itemsToRemove.add(selectedItem);
							mw.dfw.setVisible(false);
							mw.dfw = new DonateFoodWindow(options);
							mw.dfw.btnNewButton.addActionListener(this);
							mw.dfw.btnNewButton_1.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent arg0) {
									if(Utils.this.itemsToRemove.size() != 0) {
										Iterator <Item> it = itemsToRemove.iterator();
										while(it.hasNext()) {
											Item currItem = it.next();
											itemRep.delete(currItem);
										}
										
										mw.dfw.dispatchEvent(new WindowEvent(mw.dfw, WindowEvent.WINDOW_CLOSING));
									}
								}
							});
							mw.dfw.setVisible(true);
							
							Iterator <Item> it = itemsToRemove.iterator();
							while(it.hasNext()) {
								Item currItem = it.next();
								mw.dfw.addToTable(currItem.getName(), currItem.getQuantity());
							}
						}
					}
				});
				mw.dfw.btnNewButton_1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if(Utils.this.itemsToRemove.size() != 0) {
							Iterator <Item> it = itemsToRemove.iterator();
							while(it.hasNext()) {
								Item currItem = it.next();
								itemRep.delete(currItem);
							}
							
							mw.dfw.dispatchEvent(new WindowEvent(mw.dfw, WindowEvent.WINDOW_CLOSING));
						}
					}
				});
			}
		});
		
		mw.btnGenerateReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ReportFactory.generateReport(Utils.this.getItems(), mw.getComboBoxIndex());
			}
		});
		
	}
	
	private void checkStatus() {
		
		Utils.this.wipe();
		
		List <Item> items = Utils.this.getItems();
		
		Iterator <Item> it = items.iterator();
		float totalDailyCalories = 0;
		
		while(it.hasNext()) {
			Item currItem = it.next();
			if(currItem.getExpiration().compareTo(new Date(System.currentTimeMillis() + 86400000 * 2)) < 0 && currItem.getExpiration().compareTo(new Date(System.currentTimeMillis())) > 0) {
				Notification newNotification = new Notification();
				newNotification.setMessage("Item " + currItem.getName() + " is about to expire!");
				Notification newNotification2 = new Notification();
				newNotification2.setMessage("Please consider donating " + currItem.getName());
				notRep.save(newNotification);
				notRep.save(newNotification2);
			}
			else if(currItem.getExpiration().compareTo(new Date(System.currentTimeMillis())) < 0){
				Notification newNotification = new Notification();
				newNotification.setMessage("Item " + currItem.getName() + " has expired!");
				notRep.save(newNotification);
			}
			else totalDailyCalories += Utils.this.getIdealBurndownRatio(currItem);
		}
		
		if(this.calorieGoal < totalDailyCalories) {
			Notification newNotification = new Notification();
			newNotification.setMessage("There is a risk of having waste! Excess daily calories: " + (totalDailyCalories - this.calorieGoal));
			notRep.save(newNotification);
		}
		
	}
	
	private float getIdealBurndownRatio(Item item) {
		
		long diffInMillies = Math.abs(item.getExpiration().getTime() - (new Date(System.currentTimeMillis())).getTime());
	    int daysUntilExpiration = (int) TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
	    
	    return ((float) item.getCalories() / (float)(daysUntilExpiration + 1)) * item.getQuantity();
	}
	
	private static java.sql.Date convertUtilToSql(java.util.Date uDate) {
        java.sql.Date sDate = new java.sql.Date(uDate.getTime());
        return sDate;
    }
	
	public List <Item> getItems() {
		List <Item> items = new ArrayList <Item> ();
		
		items = itemRep.findAll();
		
		return items;
	}
	
	public List <String> getNamesAndQuantities() {
		List <String> itemNames = new ArrayList <String> ();
		Iterator <Item> it = this.getItems().iterator();
		
		while(it.hasNext()) {
			Item currItem = it.next();
			itemNames.add(currItem.getName() + "(" + currItem.getQuantity() + ")");
		}
		
		return itemNames;
	}
	
	public List <Notification> getNotifications(){
		List <Notification> notifications = new ArrayList <Notification> ();
		
		notifications = notRep.findAll();
		
		return notifications;
	}
	
	public void wipe() {
		notRep.deleteAll();
	}

}
