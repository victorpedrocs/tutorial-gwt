package com.ufrj.coppetec.stockwatcher.client;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class StockWatcher implements EntryPoint {
	
	private static final int REFRESH_INTERVAL = 5000;
	
	private VerticalPanel mainPanel = new VerticalPanel();
	private FlexTable stocksFlexTable = new FlexTable();
	private HorizontalPanel addPanel = new HorizontalPanel();
	private TextBox newSymbolTextBox = new TextBox();
	private Button addStockButton = new Button("Add");
	private Label lastUpdatedLabel = new Label();
	private ArrayList<String> stocks = new ArrayList<String>();
	
	/** 
	 * Entry Point Method
	 */
	@Override
	public void onModuleLoad() {
		// Create table for stock data.
		createStockTable();
		
	    // Assemble Add Stock panel.
		newSymbolTextBox.addStyleName("add-text-box");
		addPanel.add(newSymbolTextBox);
		addPanel.add(addStockButton);
		addPanel.addStyleName("add-panel");
		
	    // Assemble Main panel.
		mainPanel.add(stocksFlexTable);
		mainPanel.add(addPanel);
		mainPanel.add(lastUpdatedLabel);
				
	    // Associate the Main panel with the HTML host page.
		RootPanel.get("stocklist").add(mainPanel);

	    // Move cursor focus to the input box.
		newSymbolTextBox.setFocus(true);
		
		// Set timer to refresh list automatically
		Timer refreshTimer = new Timer() {
			
			@Override
			public void run() {
				refreshWatchList();
				
			}
		};
		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
		
		
		// Listen for mouse events on the Add Button.
		addStockButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				addStock();				
			}
		});
		
		// Listen for keyboard events on input box
		newSymbolTextBox.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					addStock();
				}
			}
		});
			
	}
	
	private void updateTable(StockPrice stockPrice){
		if (!stocks.contains(stockPrice.getSymbol())) {
			return;
		}
		
		int row = stocks.indexOf(stockPrice.getSymbol()) + 1;
		
		String priceText = NumberFormat.getFormat("#,##0.00").format(stockPrice.getPrice());
		NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
		
		String changeText = changeFormat.format(stockPrice.getChange());
		String changePercentText = changeFormat.format(stockPrice.getChangePercent());
		
		// Populate flextTable
		stocksFlexTable.setText(row, 1, priceText);
//		stocksFlexTable.setText(row, 2, changeText + "("+changePercentText + "%)");
		
		Label changeWidget = (Label) stocksFlexTable.getWidget(row, 2);
		changeWidget.setText(changeText + "(" + changePercentText + "%)");
		
		
		// Change the color of changeText based on it's value
		String changeStyleName = "no-change";
		if (stockPrice.getChangePercent() < 0.1f) {
			changeStyleName = "negative-change";
		}
		else if (stockPrice.getChangePercent() > 0.1f) {
			changeStyleName = "positive-change";
		}
		
		changeWidget.setStyleName(changeStyleName);
		
		
		
		
	}
	
	private void updateTable(StockPrice[] prices){
		for (int i = 0; i < prices.length; i++) {
			updateTable(prices[i]);
		}
		
		// Display timestamp showing last refresh
		lastUpdatedLabel.setText("Last update: "
				+ DateTimeFormat.getFormat("E, dd/MM/yyyy; h:mm a").format(new Date()));
	}
	
	private void refreshWatchList(){
		
		final double MAX_PRICE = 100.0;
		final double MAX_PRICE_CHANGE = 0.02;
		
		StockPrice[] prices = new StockPrice[stocks.size()];
		
		for (int i = 0; i < stocks.size(); i++) {
			double price  = Random.nextDouble() * MAX_PRICE;
			double change = price * MAX_PRICE_CHANGE * (Random.nextDouble() * 2.0 - 1.0);
			prices[i] = new StockPrice(stocks.get(i), price, change);
		}
		
		updateTable(prices);
		
	}
	
	private void styleRow(int row){
		stocksFlexTable.setWidget(row, 2, new Label());
		stocksFlexTable.getCellFormatter().addStyleName(row, 1, "watch-list-numeric-column");
		stocksFlexTable.getCellFormatter().addStyleName(row, 2, "watch-list-numeric-column");
		stocksFlexTable.getCellFormatter().addStyleName(row, 3, "watch-list-remove-column");
	}
	
	private void addStock(){
		final String symbol = newSymbolTextBox.getText().toUpperCase().trim();
		newSymbolTextBox.setFocus(true);
		
		// Stock must be between 1 and 10 chars that are numbers, letters or dots
		if(!symbol.matches("^[0-9A-Z&#92;&#92;.]{1,10}$")){
			Window.alert("'" + symbol + "' is not a valid symbol!");
			newSymbolTextBox.selectAll();
			return;
		}
		// Verificar se o Stock já existe
			else if (stocks.contains(symbol)) {
				Window.alert("'" + symbol + "' already exists.");
			newSymbolTextBox.selectAll();
			return;
		}
		// adicionar o stock À tabela
		
		int row = stocksFlexTable.getRowCount();
		stocks.add(symbol);
		stocksFlexTable.setText(row, 0, symbol);
		styleRow(row);
				
		
		
		// adicionar um botao para remover o stock
		Button removeStockButton = new Button("X");
		removeStockButton.addStyleDependentName("remove");
		removeStockButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				int removeIndex = stocks.indexOf(symbol);
				stocks.remove(removeIndex);
				stocksFlexTable.removeRow(removeIndex+1);	
			}
		});
		stocksFlexTable.setWidget(row, 3, removeStockButton);

		// get the stock price
		refreshWatchList();
		
		newSymbolTextBox.setText("");
	}
	
	private void createStockTable(){
		stocksFlexTable.setText(0, 0, "Symbol");
		stocksFlexTable.setText(0, 1, "Price");
		stocksFlexTable.setText(0, 2, "Change");
		stocksFlexTable.setText(0, 3, "Remove");
		
		// Add style
		stocksFlexTable.getRowFormatter().addStyleName(0, "watch-list-header");
		stocksFlexTable.addStyleName("watch-list");
		int row = 0;
		stocksFlexTable.getCellFormatter().addStyleName(row, 1, "watch-list-numeric-column");
		stocksFlexTable.getCellFormatter().addStyleName(row, 2, "watch-list-numeric-column");
		stocksFlexTable.getCellFormatter().addStyleName(row, 3, "watch-list-remove-column");
	}
	
}
