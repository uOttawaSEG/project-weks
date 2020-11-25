package com.example.servicenovigrad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.servicenovigrad.services.Service;
import com.example.servicenovigrad.userManagement.Admin;
import com.example.servicenovigrad.userManagement.Employee;

import java.util.ArrayList;
import java.util.Arrays;

public class AdminActivity extends AppCompatActivity {

    Button btnSelectEmployee, btnChooseEmployee, btnRemoveEmployee, btnDeleteEmployee, btnAddEdit;
    Button btnViewService, btnDelService;
    Spinner spinnerEmployee, spinnerViewService;
    TextView textDisplay;
    Spinner spinnerAvailable, spinnerChosen;
    Admin currentAccount;
    Employee selectedEmployee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        btnSelectEmployee= (Button) findViewById(R.id.btnSelect);
        btnChooseEmployee= (Button) findViewById(R.id.btnChoose);
        btnRemoveEmployee= (Button) findViewById(R.id.btnRemove);
        btnDeleteEmployee= (Button) findViewById(R.id.btnDelEmployee);
        btnAddEdit= (Button) findViewById(R.id.btnAddEdit);
        btnViewService= (Button) findViewById(R.id.btnViewService);
        btnDelService= (Button) findViewById(R.id.btnDelService);
        spinnerEmployee = (Spinner) findViewById(R.id.txtEmployee);
        textDisplay= (TextView) findViewById(R.id.displayMsg);
        spinnerAvailable = (Spinner) findViewById(R.id.selectAvailable);
        spinnerChosen = (Spinner) findViewById(R.id.selectChosen);
        spinnerViewService = (Spinner) findViewById(R.id.viewService);
        selectedEmployee=null;
        
        Intent intent = getIntent();

        String username = intent.getStringExtra("username");
        String password = intent.getStringExtra("password");

        NovigradDBHandler dbHandler = new NovigradDBHandler(this);
        currentAccount = (Admin) dbHandler.findAccount(username, password);

        ArrayAdapter<Employee> adapter = new ArrayAdapter<Employee>(this,
                android.R.layout.simple_spinner_dropdown_item, dbHandler.getEmployeeList());
        spinnerEmployee.setAdapter(adapter);

        ArrayAdapter<Service> serviceArrayAdapter = new ArrayAdapter<Service>(this,
                android.R.layout.simple_spinner_dropdown_item, dbHandler.getAllServices());
        spinnerViewService.setAdapter(serviceArrayAdapter);

        btnSelectEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    selectEmployee(v);
                }
                catch(Exception e){
                    textDisplay.setText(e.toString());
                }
            }
        });

        btnChooseEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    addServiceToEmployee(v);
                }
                catch(Exception e){
                    textDisplay.setText(e.toString());
                }
            }
        });

        btnRemoveEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    removeServiceFromEmployee(v);
                }
                catch(Exception e){
                    textDisplay.setText(e.toString());
                }
            }
        });

        btnDeleteEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    deleteEmployee(v);
                }
                catch(Exception e){
                    textDisplay.setText(e.toString());
                }
            }
        });

        btnViewService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    viewService(v);
                }
                catch(Exception e){
                    textDisplay.setText(e.toString());
                }
            }
        });

        btnDelService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    deleteSelectService(v);
                }
                catch(Exception e){
                    textDisplay.setText(e.toString());
                }
            }
        });

        btnAddEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    createService(v);
                }
                catch(Exception e){
                    textDisplay.setText(e.toString());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateOptions();
    }

    public void selectEmployee (View view) {
        if (spinnerEmployee.getSelectedItem()==null){
            textDisplay.setText("Choose an Employee");
            return;
        }
        selectedEmployee = (Employee) spinnerEmployee.getSelectedItem();
        updateOptions();

    }

    public void addServiceToEmployee (View view) {
        if(selectedEmployee==null){
            textDisplay.setText("You need to select an Employee");
            return;
        }
        if (spinnerAvailable.getSelectedItem()==null){
            textDisplay.setText("Choose a Service to Add");
            return;
        }

        Service selService= (Service) spinnerAvailable.getSelectedItem();

        NovigradDBHandler dbHandler = new NovigradDBHandler(this);
        dbHandler.addOffering(selectedEmployee.getUsername(),
                selService.getId());

        updateOptions();
    }

    public void removeServiceFromEmployee (View view) {
        if(selectedEmployee==null){
            textDisplay.setText("You need to select an Employee");
            return;
        }
        if (spinnerChosen.getSelectedItem()==null){
            textDisplay.setText("Choose a Service to Remove");
            return;
        }

        Service selService= (Service) spinnerChosen.getSelectedItem();

        NovigradDBHandler dbHandler = new NovigradDBHandler(this);
        dbHandler.deleteOffering(selectedEmployee.getUsername(),
                selService.getId());

        updateOptions();
    }

    public void deleteEmployee (View view) {
        if (selectedEmployee==null){
            textDisplay.setText("Choose an Employee");
            return;
        }
        NovigradDBHandler dbHandler = new NovigradDBHandler(this);
        dbHandler.deleteEmployee(selectedEmployee.getUsername(),selectedEmployee.getPassword());

        ArrayAdapter<Employee> adapter = new ArrayAdapter<Employee>(this,
                android.R.layout.simple_spinner_dropdown_item, dbHandler.getEmployeeList());
        spinnerEmployee.setAdapter(adapter);

        selectedEmployee=null;
        updateOptions();

    }

    public void updateOptions(){
        NovigradDBHandler dbHandler = new NovigradDBHandler(this);
        ArrayAdapter<Service> serviceArrayAdapter = new ArrayAdapter<Service>(this,
                android.R.layout.simple_spinner_dropdown_item, dbHandler.getAllServices());
        spinnerViewService.setAdapter(serviceArrayAdapter);

        if(selectedEmployee==null){
            String[] emptyList = new String[0];
            Arrays.fill(emptyList, null);
            ArrayAdapter emptyAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, emptyList);
            spinnerAvailable.setAdapter(emptyAdapter);
            spinnerChosen.setAdapter(emptyAdapter);
            return;
        }

        ArrayList<Service> all= dbHandler.getAllServices();

        ArrayList<String[]> offerings = dbHandler.findOfferings(selectedEmployee.getUsername());
        ArrayList<Service> offered = new ArrayList<Service>();
        ArrayList<Service> available = new ArrayList<Service>();


        for (Service service : all){
            boolean isInOfferings=false;
            for (String[] offering : offerings){
                if (service.getName().equals(offering[0])){isInOfferings=true;}
            }
            if(isInOfferings){ offered.add(service); }
            else{available.add(service);}
        }

        ArrayAdapter<Service> chosenAdapter = new ArrayAdapter<Service>(this,
                android.R.layout.simple_spinner_dropdown_item, offered);

        spinnerChosen.setAdapter(chosenAdapter);

        ArrayAdapter<Service> availableAdapter = new ArrayAdapter<Service>(this,
                android.R.layout.simple_spinner_dropdown_item, available);
        spinnerAvailable.setAdapter(availableAdapter);
    }

    public void viewService(View view){
        if (spinnerViewService.getSelectedItem()==null){
            textDisplay.setText("Select a service to view");
            return;
        }

        Intent serviceIntent = new Intent(this, ServiceActivity.class);
        Service service = (Service) spinnerViewService.getSelectedItem();
        serviceIntent.putExtra("serviceName", service.getName());
        startActivity(serviceIntent);
    }

    public void deleteSelectService(View view){
        if (spinnerViewService.getSelectedItem()==null){
            textDisplay.setText("Select a service to view");
            return;
        }

        NovigradDBHandler dbHandler = new NovigradDBHandler(this);
        Service selected = (Service) spinnerViewService.getSelectedItem();
        dbHandler.deleteService(selected.getName());

        ArrayAdapter<Service> serviceArrayAdapter = new ArrayAdapter<Service>(this,
                android.R.layout.simple_spinner_dropdown_item, dbHandler.getAllServices());
        spinnerViewService.setAdapter(serviceArrayAdapter);
        updateOptions();
    }

    public void createService(View view) {

        Intent editServiceIntent = new Intent(this, EditServiceActivity.class);
        startActivity(editServiceIntent);
    }

}