package com.example.myapplication;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class LoginAdapter extends FragmentPagerAdapter {
    private Context context;
    int totalTabs;
    public LoginAdapter(FragmentManager fm, Context context, int totalTabs)
    {
        super(fm);
        this.context=context;
        this.totalTabs=totalTabs;
    }
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                PatientLoginFragmentTab fpt=new PatientLoginFragmentTab();
                return fpt;
            case 1:
                DoctorLoginFragmentTab fdt=new DoctorLoginFragmentTab();
                return fdt;
            default:
                return null;
        }
    }
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Login as Student";
            case 1:
                return "Login as Admin";
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return totalTabs;
    }
}
