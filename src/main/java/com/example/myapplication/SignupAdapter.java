package com.example.myapplication;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SignupAdapter extends FragmentPagerAdapter {
    private Context context;
    int totalTabs;
    public SignupAdapter(FragmentManager fm, Context context, int totalTabs)
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
                PatientSignUpFragmentTab fpt=new PatientSignUpFragmentTab();
                return fpt;
            case 1:
                DoctorSignupFragmentTab fdt=new DoctorSignupFragmentTab();
                return fdt;
            default:
                return null;
        }
    }
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "SignUp as Student";
            case 1:
                return "SignUp as Admin";
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return totalTabs;
    }
}
