package com.breadwallet.presenter.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.breadwallet.R;
import com.breadwallet.presenter.entities.TransactionItem;
import com.breadwallet.presenter.interfaces.BRAuthCompletion;
import com.breadwallet.tools.security.AuthManager;
import com.breadwallet.tools.security.PostAuth;
import com.breadwallet.tools.threads.BRExecutor;
import com.breadwallet.tools.util.BRCurrency;
import com.breadwallet.wallet.BRWalletManager;

import java.math.BigDecimal;

public class FragmentStake extends Fragment {

    private TextView stakingStatus;
    private TextView stakedBalance;
    private EditText stakerAddressInput;
    private Button buttonStake;
    private Button buttonUnstake;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stake, container, false);

        stakingStatus = rootView.findViewById(R.id.staking_status);
        stakedBalance = rootView.findViewById(R.id.staked_balance);
        stakerAddressInput = rootView.findViewById(R.id.staker_address_input);
        buttonStake = rootView.findViewById(R.id.button_stake);
        buttonUnstake = rootView.findViewById(R.id.button_unstake);

        updateUI();

        buttonStake.setOnClickListener(v -> {
            String stakerAddr = stakerAddressInput.getText().toString().trim();
            if (stakerAddr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a staker address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!BRWalletManager.validateAddress(stakerAddr)) {
                Toast.makeText(getContext(), "Invalid staker address", Toast.LENGTH_SHORT).show();
                return;
            }

            performStaking(stakerAddr);
        });

        buttonUnstake.setOnClickListener(v -> {
            performUnstaking();
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void performStaking(String stakerAddr) {
        final Activity activity = getActivity();
        if (activity == null) return;
        
        long balance = BRWalletManager.getInstance().nativeBalance();
        long alreadyStaked = BRWalletManager.getInstance().stakedBalance();
        long spendable = balance - alreadyStaked;

        if (spendable <= 1000000) { // 0.01 AETH minimum buffer for fees
            Toast.makeText(getContext(), "Insufficient spendable balance to start staking", Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerAddr = BRWalletManager.getReceiveAddress();
        
        new Thread(() -> {
            long fee = BRWalletManager.getInstance().feeForTransactionAmount(spendable);
            if (fee <= 0) fee = 2000000;
            else fee += 500000; // safety margin for cold stake output script size & change output
            
            long amountToStake = spendable - fee;
            byte[] txBytes = null;
            
            for (int attempt = 0; attempt < 5 && txBytes == null && amountToStake > 1000000; attempt++) {
                txBytes = BRWalletManager.getInstance().tryStakingTransaction(ownerAddr, stakerAddr, amountToStake);
                if (txBytes == null) {
                    amountToStake -= 500000;
                }
            }

            final long finalAmountToStake = amountToStake;
            final byte[] finalTxBytes = txBytes;

            activity.runOnUiThread(() -> {
                if (finalTxBytes != null) {
                    TransactionItem item = new TransactionItem(stakerAddr, null, finalAmountToStake, null, false, "Staking");
                    item.serializedTx = finalTxBytes;
                    PostAuth.getInstance().setTransactionItem(item);

                    BigDecimal amountAeth = new BigDecimal(finalAmountToStake).divide(new BigDecimal(100000000), 8, BigDecimal.ROUND_HALF_EVEN);
                    String formattedAmount = BRCurrency.getFormattedCurrencyString(activity, "AETH", amountAeth);

                    AuthManager.getInstance().authPrompt(
                        activity,
                        "",
                        "Confirm Staking " + formattedAmount,
                        false,
                        false,
                        new BRAuthCompletion() {
                            @Override
                            public void onComplete() {
                                BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(() -> {
                                    PostAuth.getInstance().onPublishTxAuth(activity, false);
                                    BRExecutor.getInstance().forMainThreadTasks().execute(() -> {
                                        Toast.makeText(activity, "Staking transaction published successfully!", Toast.LENGTH_LONG).show();
                                        updateUI();
                                    });
                                });
                            }

                            @Override
                            public void onCancel() {
                            }
                        }
                    );
                } else {
                    Toast.makeText(activity, "Failed to create staking transaction. Check balance and fee.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void performUnstaking() {
        final Activity activity = getActivity();
        if (activity == null) return;

        long staked = BRWalletManager.getInstance().stakedBalance();
        if (staked <= 0) {
            Toast.makeText(getContext(), "No staked balance to unstake", Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerAddr = BRWalletManager.getReceiveAddress();
        long fee = BRWalletManager.getInstance().feeForTransaction(ownerAddr, staked);
        long amountToUnstake = staked - fee;

        if (amountToUnstake <= 0) {
            Toast.makeText(getContext(), "Staked balance is too low to cover transaction fee", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            byte[] txBytes = BRWalletManager.getInstance().tryTransaction(ownerAddr, amountToUnstake);

            activity.runOnUiThread(() -> {
                if (txBytes != null) {
                    TransactionItem item = new TransactionItem(ownerAddr, null, amountToUnstake, null, false, "Unstaking");
                    item.serializedTx = txBytes;
                    PostAuth.getInstance().setTransactionItem(item);

                    BigDecimal amountAeth = new BigDecimal(amountToUnstake).divide(new BigDecimal(100000000), 8, BigDecimal.ROUND_HALF_EVEN);
                    String formattedAmount = BRCurrency.getFormattedCurrencyString(activity, "AETH", amountAeth);

                    AuthManager.getInstance().authPrompt(
                        activity,
                        "",
                        "Confirm Unstaking " + formattedAmount,
                        false,
                        false,
                        new BRAuthCompletion() {
                            @Override
                            public void onComplete() {
                                BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(() -> {
                                    PostAuth.getInstance().onPublishTxAuth(activity, false);
                                    BRExecutor.getInstance().forMainThreadTasks().execute(() -> {
                                        Toast.makeText(activity, "Unstaking transaction published successfully!", Toast.LENGTH_LONG).show();
                                        updateUI();
                                    });
                                });
                            }

                            @Override
                            public void onCancel() {
                            }
                        }
                    );
                } else {
                    Toast.makeText(activity, "Failed to create unstaking transaction.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void updateUI() {
        if (getActivity() == null) return;

        long balance = BRWalletManager.getInstance().nativeBalance();
        long staked = BRWalletManager.getInstance().stakedBalance();
        
        BigDecimal stakedAeth = new BigDecimal(staked).divide(new BigDecimal(100000000), 8, BigDecimal.ROUND_HALF_EVEN);
        
        stakedBalance.setText(BRCurrency.getFormattedCurrencyString(getActivity(), "AETH", stakedAeth));
        
        if (staked > 0) {
            stakingStatus.setText("Staking Active");
            stakingStatus.setTextColor(getResources().getColor(R.color.litecoin_litewallet_blue));
            buttonUnstake.setVisibility(View.VISIBLE);
        } else {
            stakingStatus.setText("Ready to Stake");
            stakingStatus.setTextColor(getResources().getColor(R.color.light_gray));
            buttonUnstake.setVisibility(View.GONE);
        }
    }
}
