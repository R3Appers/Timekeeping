package com.rrreyes.prototype.timekeeping.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rrreyes.prototype.timekeeping.CameraActivity;
import com.rrreyes.prototype.timekeeping.Constants.Constants;
import com.rrreyes.prototype.timekeeping.Constants.RetrofitData;
import com.rrreyes.prototype.timekeeping.Constants.SharedData;
import com.rrreyes.prototype.timekeeping.DTRActivity;
import com.rrreyes.prototype.timekeeping.Interfaces.TKService;
import com.rrreyes.prototype.timekeeping.MainActivity;
import com.rrreyes.prototype.timekeeping.Models.Employee;
import com.rrreyes.prototype.timekeeping.Models.EmployeeData;
import com.rrreyes.prototype.timekeeping.Models.EmployeeInfo;
import com.rrreyes.prototype.timekeeping.Models.Login;
import com.rrreyes.prototype.timekeeping.Models.LoginCredentials;
import com.rrreyes.prototype.timekeeping.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by R. R. Reyes on 12/21/2017.
 */

public class TKDialogs {

    private Retrofit retrofit;
    private TKService service;

    private Context context;

    private Dialog dialog;

    private EditText Username;
    private EditText Password;
    private Button Btn_Submit;

    EditText BranchID;
    EditText EmpIDN;
    Button Btn_SyncData;
    Button Btn_SendDTR;
    Button Btn_Logout;
    Button Btn_ClockIn;
    Button Btn_Confirm;

    TextView TV_EmpIDN;
    TextView TV_EmpName;
    TextView TV_EmpLog;

    List<EmployeeInfo> empi;
    EmployeeInfo empInfo;

    Realm realm;
    private SharedData sd;

    public TKDialogs(Context context) {
        this.context = context;
        this.sd = new SharedData(context);

        Realm.init(context);

        realm = Realm.getDefaultInstance();
    }

    public Dialog LoginDialog() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_admin_login);
        dialog.setCancelable(false);
        Username = dialog.findViewById(R.id.Username);
        Password = dialog.findViewById(R.id.Password);
        Btn_Submit = dialog.findViewById(R.id.Btn_Submit);
        Btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Btn_Submit.setEnabled(false);
                Btn_Submit.setText(Constants.WAIT);
                retrofit = RetrofitData.newInstance;
                service = retrofit.create(TKService.class);
                LoginCredentials login = new LoginCredentials();
                login.setUsername(Username.getText().toString().trim());
                login.setPassword(Password.getText().toString().trim());
                service.loginUser(login).enqueue(new Callback<Login>() {
                    @Override
                    public void onResponse(Call<Login> call, Response<Login> response) {
                        Btn_Submit.setEnabled(true);
                        Btn_Submit.setText(R.string.login);
                        dialog.dismiss();
                        Login login = null;
                        if(response.body() != null) {
                            login = response.body();
                        }
                        if(login != null) {
                            if(login.getCode() == 200) {
                                sd.SetCompanyID(login.getData().get(0).getCompanyID());
                                sd.SetToken(login.getToken());
                                sd.SetUserID(login.getData().get(0).getID());

                                dialog = BranchConfig();
                                dialog.show();
                            } else {
                                Toast.makeText(context, Constants.FAIL_LOGIN, Toast.LENGTH_LONG).show();
                                Log.e(Constants.DENIED, login.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Login> call, Throwable t) {
                        Btn_Submit.setEnabled(true);
                        Btn_Submit.setText(R.string.login);
                        dialog.dismiss();
                        Toast.makeText(context, Constants.ERROR_LOGIN, Toast.LENGTH_LONG).show();
                        Log.e(Constants.DENIED, t.getMessage());
                    }
                });
            }
        });

        return dialog;
    }

    private Dialog BranchConfig() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_admin_config);
        dialog.setCancelable(true);
        BranchID = dialog.findViewById(R.id.BranchID);
        Btn_SyncData = dialog.findViewById(R.id.Btn_SyncData);
        Btn_SendDTR = dialog.findViewById(R.id.Btn_SendDTR);
        Btn_Logout = dialog.findViewById(R.id.Btn_Logout);

        if(sd.GetBranchID() != 0) {
            BranchID.setText(sd.GetBranchID() + "");
        }

        if(sd.GetFirstTime()) {
            Btn_SendDTR.setEnabled(false);
            Btn_SendDTR.setVisibility(View.GONE);
        } else {
            Btn_SendDTR.setEnabled(true);
            Btn_SendDTR.setVisibility(View.VISIBLE);
        }

        Btn_SyncData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Btn_SyncData.setEnabled(false);
                Btn_SendDTR.setEnabled(false);
                Btn_SyncData.setText(Constants.WAIT);
                Btn_SendDTR.setText(Constants.WAIT);
                if((!BranchID.getText().toString().trim().equals(""))
                        && (!BranchID.getText().toString().trim().equals(" "))
                        && (BranchID.getText().toString().trim() != null)) {
                    sd.SetBranchID(Integer.parseInt(BranchID.getText().toString().trim()));
                    service = RetrofitData.newInstance(context, sd.GetToken());
                    service.getAllEmployees(sd.GetCompanyID()).enqueue(new Callback<Employee>() {
                        @Override
                        public void onResponse(Call<Employee> call, Response<Employee> response) {
                            Btn_SyncData.setEnabled(true);
                            Btn_SendDTR.setEnabled(true);
                            Btn_SyncData.setText(R.string.syncdata);
                            Btn_SendDTR.setText(R.string.senddtr);
                            Employee emp = null;
                            if(response.body() != null) {
                                emp = response.body();
                            }
                            if(emp != null) {
                                if(emp.getCode() == 200) {
                                    List<EmployeeData> emps = emp.getData();
                                    empi = new ArrayList<>();
                                    for(EmployeeData emd : emps) {
                                        EmployeeInfo tempi = new EmployeeInfo();
                                        tempi.setBarcode(emd.getBarcode());
                                        tempi.setBranchID(emd.getBranchID());
                                        tempi.setBranchName(emd.getBranchName());
                                        tempi.setFirstName(emd.getFirstName());
                                        tempi.setMiddleName(emd.getMiddleName());
                                        tempi.setLastName(emd.getLastName());
                                        tempi.setPosition(emd.getPosition());
                                        empi.add(tempi);
                                    }

                                    Realm.init(context);

                                    Realm realm = Realm.getDefaultInstance();

                                    realm.executeTransactionAsync(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            realm.copyToRealmOrUpdate(empi);
                                        }
                                    }, new Realm.Transaction.OnSuccess() {
                                        @Override
                                        public void onSuccess() {
                                            Btn_SyncData.setEnabled(true);
                                            Btn_SendDTR.setEnabled(true);
                                            Btn_SyncData.setText(R.string.syncdata);
                                            Btn_SendDTR.setText(R.string.senddtr);
                                            Toast.makeText(context, Constants.SUCCESS_SYNC, Toast.LENGTH_LONG).show();
                                            sd.SetFirstTime(false);
                                        }
                                    }, new Realm.Transaction.OnError() {
                                        @Override
                                        public void onError(Throwable error) {
                                            Btn_SyncData.setEnabled(true);
                                            Btn_SendDTR.setEnabled(true);
                                            Btn_SyncData.setText(R.string.syncdata);
                                            Btn_SendDTR.setText(R.string.senddtr);
                                            Toast.makeText(context, Constants.FAIL_SYNC2, Toast.LENGTH_LONG).show();
                                            Log.e(Constants.DENIED, error.getMessage());
                                        }
                                    });
                                } else {
                                    Btn_SyncData.setEnabled(true);
                                    Btn_SendDTR.setEnabled(true);
                                    Btn_SyncData.setText(R.string.syncdata);
                                    Btn_SendDTR.setText(R.string.senddtr);
                                    Toast.makeText(context, Constants.FAIL_SYNC2, Toast.LENGTH_LONG).show();
                                    Log.e(Constants.DENIED, emp.getMessage());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Employee> call, Throwable t) {
                            Btn_SyncData.setEnabled(true);
                            Btn_SendDTR.setEnabled(true);
                            Btn_SyncData.setText(R.string.syncdata);
                            Btn_SendDTR.setText(R.string.senddtr);
                            Toast.makeText(context, Constants.ERROR_SYNC, Toast.LENGTH_LONG).show();
                            Log.e(Constants.DENIED, t.getMessage());
                        }
                    });
                } else {
                    Btn_SyncData.setEnabled(true);
                    Btn_SendDTR.setEnabled(true);
                    Btn_SyncData.setText(R.string.syncdata);
                    Btn_SendDTR.setText(R.string.senddtr);
                    Toast.makeText(context, Constants.FAIL_SYNC, Toast.LENGTH_LONG).show();
                }
            }
        });
        Btn_SendDTR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((!BranchID.getText().toString().trim().equals(""))
                        && (!BranchID.getText().toString().trim().equals(" "))
                        && (BranchID.getText().toString().trim() != null)) {
                    Intent i = new Intent(context, DTRActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(context, Constants.FAIL_SYNC, Toast.LENGTH_LONG).show();
                }
            }
        });
        Btn_Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent i = new Intent(context, MainActivity.class);
                context.startActivity(i);
            }
        });

        return dialog;
    }

    public Dialog ForgotID(int type) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_scan_input);
        dialog.setCancelable(true);
        EmpIDN = dialog.findViewById(R.id.EmpIDN);
        Btn_ClockIn = dialog.findViewById(R.id.Btn_ClockIn);

        final int timeType = type;
        EmpIDN.setText("PAS20");

        Btn_ClockIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempid = EmpIDN.getText().toString().trim();
                empInfo = null;
                Btn_ClockIn.setEnabled(false);
                Btn_ClockIn.setText(Constants.WAIT);
                if((!tempid.equals(""))
                        && (!tempid.equals(" "))) {
                    Calendar cal = Calendar.getInstance();
                    String currentDate = Constants.DATE_FORMAT.format(new Date());
                    String currentTime = Constants.TIME_FORMAT.format(cal.getTime());

                    try {
                        realm.beginTransaction();
                        empInfo = realm
                                .where(EmployeeInfo.class)
                                .equalTo("Barcode",
                                        tempid)
                                .findFirst();
                        realm.commitTransaction();
                        if(empInfo != null) {
                            Btn_ClockIn.setEnabled(true);
                            Btn_ClockIn.setText(R.string.clockin);
                            dialog.dismiss();

                            dialog = ConfirmID(empInfo, currentDate, currentTime, timeType);
                            dialog.show();
                        } else {
                            Toast.makeText(context, "ID doesn't exist.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    } catch (Exception ex) {
                        Log.e(Constants.LOG_TAG, ex.getMessage());
                        Btn_ClockIn.setEnabled(true);
                        Btn_ClockIn.setText(R.string.clockin);
                    }
                }
            }
        });

        return dialog;
    }

    private Dialog ConfirmID(EmployeeInfo info, String date, String time, int type) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_scan_confirm);
        dialog.setCancelable(false);
        TV_EmpIDN = dialog.findViewById(R.id.TV_EmpIDN);
        TV_EmpName = dialog.findViewById(R.id.TV_EmpName);
        TV_EmpLog = dialog.findViewById(R.id.TV_EmpLog);
        Btn_Confirm = dialog.findViewById(R.id.Btn_Confirm);

        final EmployeeInfo empInfo = info;
        final String name = new StringBuilder()
                .append(empInfo.getFirstName().toUpperCase())
                .append(" ")
                .append(empInfo.getMiddleName().toUpperCase())
                .append(" ")
                .append(empInfo.getLastName().toUpperCase())
                .toString();
        StringBuilder logBuilder = new StringBuilder();
        switch (type) {
            case 1 :
                logBuilder.append("TIME IN");
                break;
            case 2 :
                logBuilder.append("BREAK OUT");
                break;
            case 3 :
                logBuilder.append("BREAK IN");
                break;
            case 4 :
                logBuilder.append("TIME OUT");
                break;
        }
        logBuilder
                .append(" : ")
                .append(date)
                .append(" - ")
                .append(time);
        final String currentDate = date;
        final String currentTime = time;
        final int timeType = type;

        TV_EmpIDN.setText(empInfo.getBarcode());
        TV_EmpName.setText(name);
        TV_EmpLog.setText(logBuilder.toString());

        Btn_Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent i = new Intent(context, CameraActivity.class);
                    i.putExtra(Constants.TAG_BARCODE, empInfo.getBarcode());
                    i.putExtra(Constants.TAG_NAME, name);
                    i.putExtra(Constants.TAG_DATE, currentDate);
                    i.putExtra(Constants.TAG_TIME, currentTime);
                    i.putExtra(Constants.TAG_TYPE, timeType + "");
                    context.startActivity(i);
                } catch (Exception ex) {
                    Log.e(Constants.LOG_TAG, ex.getMessage());
                }
            }
        });

        return dialog;
    }
}
