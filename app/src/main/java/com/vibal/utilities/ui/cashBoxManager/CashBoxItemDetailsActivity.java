package com.vibal.utilities.ui.cashBoxManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.vibal.utilities.R;
import com.vibal.utilities.databinding.CashBoxDetailsItemBinding;
import com.vibal.utilities.databinding.CashBoxItemDetailsActivityBinding;
import com.vibal.utilities.databinding.CashBoxItemDetailsFragmentBinding;
import com.vibal.utilities.models.CashBoxInfo;
import com.vibal.utilities.models.EntryBase;
import com.vibal.utilities.models.EntryInfo;
import com.vibal.utilities.models.Participant;
import com.vibal.utilities.ui.NameSelectSpinner;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.Util;
import com.vibal.utilities.viewModels.CashBoxDetailsViewModel;
import com.vibal.utilities.workaround.LinearLayoutManagerWrapper;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CashBoxItemDetailsActivity extends AppCompatActivity {
    public static final String CASHBOX_ID_EXTRA = "com.vibal.utilities.ui.cashBoxManager.details.cashBoxId";
    public static final String CASHBOX_TYPE_EXTRA = "com.vibal.utilities.ui.cashBoxManager.details.cashBoxType";
    public static final String ENTRY_POSITION_EXTRA = "com.vibal.utilities.ui.cashBoxManager.details.entryPosition";

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager2 viewPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private ItemDetailsPagerAdapter pagerAdapter;
    private CashBoxItemDetailsActivityBinding binding;
    private CashBoxDetailsViewModel viewModel;
    private final NumberFormat formatCurrency = NumberFormat.getCurrencyInstance();
    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final HashSet<Participant> participantsChanged = new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CashBoxItemDetailsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get intent
        Intent intent = getIntent();
        @CashBoxType.Type int cashBoxType = intent.getIntExtra(CASHBOX_TYPE_EXTRA, -1);
        long cashBoxId = intent.getLongExtra(CASHBOX_ID_EXTRA, CashBoxInfo.NO_ID);
        int entryPosition = intent.getIntExtra(ENTRY_POSITION_EXTRA, -1);
        if (cashBoxType == -1 || cashBoxId == CashBoxInfo.NO_ID || entryPosition == -1)
            throw new IllegalArgumentException("Intent does not contain correct extras");

        // Get viewModel
        viewModel = new ViewModelProvider(this,
                CashBoxDetailsViewModel.Factory.getInstance(getApplication(),
                        CashBoxType.getCashBoxRepositoryClass(cashBoxType), cashBoxId))
                .get(CashBoxDetailsViewModel.class);

        // Set currency
        compositeDisposable.add(viewModel.getCurrency()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(currency -> {
                    if (!Objects.equals(formatCurrency.getCurrency(), currency)) {
                        formatCurrency.setCurrency(currency);
                    }
                }));

        //Set Toolbar as ActionBar
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = binding.pager;
        pagerAdapter = new ItemDetailsPagerAdapter(this, entryPosition + 1);
        viewModel.getCashBox().observe(this, cashBox ->
                pagerAdapter.size = cashBox.getEntries().size());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(entryPosition, false);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // When new entry selected, clear saved changes
                Util.clearFocus(CashBoxItemDetailsActivity.this);
                participantsChanged.clear();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_cashbox_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // imp make save not dependent in clicking button
        if (item.getItemId() == R.id.action_save) {
            Util.clearFocus(this);  // First clear focus so any changes are saved
//            if (participantsChanged.isEmpty())
//                Toast.makeText(this, "No changes made", Toast.LENGTH_SHORT).show();

            Completable completable = Completable.complete();
            for (Participant p : participantsChanged) {
                completable = completable.andThen(viewModel.updateParticipant(p));
            }
            compositeDisposable.add(completable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() ->
                                    Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show(),
                            throwable -> {
                                LogUtil.error("", throwable);
                                Toast.makeText(this, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }));
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    //    @Override
//    public void onBackPressed() {
//        if (viewPager.getCurrentItem() == 0) {
//            // If the user is currently looking at the first step, allow the system to handle the
//            // Back button. This calls finish() on this activity and pops the back stack.
//            super.onBackPressed();
//        } else {
//            // Otherwise, select the previous step.
//            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
//        }
//    }

    private static class ItemDetailsPagerAdapter extends FragmentStateAdapter {
        private int size;

        public ItemDetailsPagerAdapter(@NonNull FragmentActivity fragmentActivity, int size) {
            super(fragmentActivity);
            this.size = size;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return ItemDetailsFragment.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return size;
        }
    }

    public static class ItemDetailsFragment extends Fragment {
        private static final String POSITION_ARG = "posArg";

        private final DiffUtil.ItemCallback<Participant> DIFF_CALLBACK =
                new DiffUtil.ItemCallback<Participant>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull Participant oldItem,
                                                   @NonNull Participant newItem) {
                        return oldItem.areItemsTheSame(newItem);
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull Participant oldItem,
                                                      @NonNull Participant newItem) {
                        return oldItem.areContentsTheSame(oldItem);
                    }
                };

        private CashBoxItemDetailsFragmentBinding binding;
        private ItemDetailsRecyclerAdapter adapterFrom;
        private ItemDetailsRecyclerAdapter adapterTo;
        private int position;
        private long entryId;

        @NonNull
        public static ItemDetailsFragment newInstance(int position) {
            ItemDetailsFragment fragment = new ItemDetailsFragment();
            Bundle bundle = fragment.getArguments() != null ? fragment.getArguments() : new Bundle();
            bundle.putInt(POSITION_ARG, position);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            position = getArguments().getInt(POSITION_ARG);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            binding = CashBoxItemDetailsFragmentBinding.inflate(inflater, container, false);
            return binding.getRoot();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            binding = null;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Set up RecyclerView
            binding.rvFrom.setHasFixedSize(false);
            binding.rvTo.setHasFixedSize(false);
            binding.rvFrom.setLayoutManager(new LinearLayoutManagerWrapper(requireContext()));   // Workaround for recycler error
            binding.rvTo.setLayoutManager(new LinearLayoutManagerWrapper(requireContext()));   // Workaround for recycler error
            adapterFrom = new ItemDetailsRecyclerAdapter();
            adapterTo = new ItemDetailsRecyclerAdapter();
            binding.rvFrom.setAdapter(adapterFrom);
            binding.rvTo.setAdapter(adapterTo);

            // Set up listeners
            binding.titleFrom.setOnClickListener(view1 -> toggleExpandableView(binding.elFrom, binding.addImageFrom));
            binding.titleTo.setOnClickListener(view1 -> toggleExpandableView(binding.elTo, binding.addImageTo));
            binding.addImageFrom.setOnClickListener(v -> {
                if (binding.elFrom.isExpanded())
                    showAddParticipantDialog(binding.addImageFrom, Participant::newFrom);
                else
                    toggleExpandableView(binding.elFrom, binding.addImageFrom);
            });
            binding.addImageTo.setOnClickListener(v -> {
                if (binding.elTo.isExpanded())
                    showAddParticipantDialog(binding.addImageTo, Participant::newTo);
                else
                    toggleExpandableView(binding.elTo, binding.addImageTo);
            });
        }

        private void toggleExpandableView(@NonNull ExpandableLayout layout, ImageView imageView) {
            layout.toggle();
            if (layout.isExpanded())
                imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add));
            else
                imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ms__arrow));
        }

        private void showAddParticipantDialog(View anchor, Function<String, Participant> create) {
            // Show pop-up with names
            PopupMenu popupMenu = new PopupMenu(requireContext(), anchor);
            Menu menu = popupMenu.getMenu();
            final int ITEM_ID_STRING_ADD = 1;
            menu.add(Menu.NONE, ITEM_ID_STRING_ADD, 0, NameSelectSpinner.STRING_ADD);
            for (String name : getDetailsActivity().viewModel.requireCashBox().getCacheNames()) {
                menu.add(name);
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == ITEM_ID_STRING_ADD) {
                    // Show dialog to add new names
                    NameSelectSpinner.createAddParticipantDialog(requireContext(), (dialog, inputName, layoutName) -> {
                        getDetailsActivity().compositeDisposable.add(
                                getDetailsActivity().viewModel.insertParticipant(entryId,
                                        create.apply(inputName.getText().toString()))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(() -> {
                                        }, throwable ->
                                        {
                                            LogUtil.error("", throwable);
                                            Toast.makeText(getContext(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        })
                        );
                        dialog.dismiss();
                    }).show();
                } else {
                    getDetailsActivity().compositeDisposable.add(
                            getDetailsActivity().viewModel.insertParticipant(entryId,
                                    create.apply(item.getTitle().toString()))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                    }, throwable ->
                                    {
                                        LogUtil.error("", throwable);
                                        Toast.makeText(getContext(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    })
                    );
                }
                return true;
            });
            popupMenu.show();

//            NameSelectSpinner.createAddParticipantDialog(requireContext(), (dialog, inputName, layoutName) -> {
//                getDetailsActivity().compositeDisposable.add(
//                        getDetailsActivity().viewModel.insertParticipant(entryId,
//                                create.apply(inputName.getText().toString()))
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe()
//                );
//                dialog.dismiss();
//            }).show();
        }

        @NonNull
        private CashBoxItemDetailsActivity getDetailsActivity() {
            return ((CashBoxItemDetailsActivity) requireActivity());
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            getDetailsActivity().viewModel.getCashBox()
                    .observe(getViewLifecycleOwner(), cashBox -> {
                        EntryBase<?> entry = cashBox.getEntries().get(position);
                        EntryInfo entryInfo = entry.getEntryInfo();
                        entryId = entryInfo.getId();
                        adapterFrom.submitList(entry.getFromParticipants());
                        adapterTo.submitList(entry.getToParticipants());

                        // Set viewTexts
                        // Amount
                        Util.formatAmountTextView(binding.itemAmount,
                                getDetailsActivity().formatCurrency, entryInfo.getAmount(), false);
//                        binding.itemAmount.setText(getDetailsActivity().formatCurrency.format(
//                                entryInfo.getAmount()));

                        // Date
                        binding.itemDate.setText(getDetailsActivity().dateFormat.format(
                                entryInfo.getDate().getTime()));
                        // Info
                        binding.itemInfo.setText(entryInfo.printInfo());
                        // Amount balance
                        double balance = entry.getParticipantBalance(
                                Participant.createDefaultParticipant(entryInfo.getId(), true));
                        Util.formatAmountTextView(binding.itemBalance,
                                getString(R.string.between_parenthesis,
                                        getDetailsActivity().formatCurrency.format(balance)),
                                balance, true);
                    });
        }

        private class ItemDetailsRecyclerAdapter extends ListAdapter<Participant, ItemDetailsRecyclerAdapter.ViewHolder> {
            public ItemDetailsRecyclerAdapter() {
                super(DIFF_CALLBACK);
            }

            @NonNull
            @Override
            public ItemDetailsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                CashBoxDetailsItemBinding binding = CashBoxDetailsItemBinding.inflate(
                        LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
                return new ViewHolder(binding.getRoot(), binding);
            }

            @Override
            public void onBindViewHolder(@NonNull ItemDetailsRecyclerAdapter.ViewHolder holder, int position) {
                Participant participant = getItem(position);
                holder.binding.rvName.setText(participant.printName());
                holder.setAmount(participant.getAmount());
            }

            private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                    View.OnLongClickListener, View.OnFocusChangeListener {
                private final CashBoxDetailsItemBinding binding;

                public ViewHolder(@NonNull View itemView, CashBoxDetailsItemBinding binding) {
                    super(itemView);
                    this.binding = binding;
                    binding.deleteImage.setOnClickListener(this);
                    itemView.setOnLongClickListener(this);
                    binding.inputTextAmount.setOnFocusChangeListener(this);
                }

                private void setAmount(double amount) {
                    binding.inputTextAmount.setText(String.format(Locale.US, "%.2f", amount));
                    if (amount < 0)
                        binding.inputTextAmount.setTextColor(requireContext().getColor(R.color.colorNegativeNumber));
                    else
                        binding.inputTextAmount.setTextColor(requireContext().getColor(R.color.colorNeutralNumber));
                }

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus)
                        return;

                    String input = binding.inputTextAmount.getText().toString().trim();
                    Participant oldPart = getItem(getBindingAdapterPosition());
                    double amount = oldPart.getAmount();

                    if (!input.isEmpty()) {
                        try {
                            amount = Util.parseExpression(input);
                            getDetailsActivity().participantsChanged.add(oldPart.clone(amount));
                        } catch (NumberFormatException e) {
                            binding.inputLayoutAmount.setError(getString(R.string.errorMessageAmount));
                            binding.inputTextAmount.selectAll();
                            Util.showKeyboard(requireContext(), binding.inputTextAmount);
                        }
                    }

                    setAmount(Math.signum(oldPart.getAmount()) * Math.abs(amount));
                }

                @Override
                public void onClick(View v) {
                    getDetailsActivity().compositeDisposable.add(
                            getDetailsActivity().viewModel.deleteParticipant(getItem(getBindingAdapterPosition()))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> Toast.makeText(requireContext(), "Participant deleted", Toast.LENGTH_SHORT).show(),
                                            throwable -> {
                                                LogUtil.error("", throwable);
                                                Toast.makeText(getContext(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            })
                    );
                }

                @Override
                public boolean onLongClick(View v) {
                    // imp add option to delete everywhere
//                    new MyDialogBuilder(requireContext())
//                            .setTitle(R.string.deleteEverywhere)
//                            .setMessage(R.string.deleteParticipantEverywhere)
//                            .setPositiveButton((dialog, which) -> getDetailsActivity().compositeDisposable.add(
//                                    getDetailsActivity().viewModel.deleteParticipantFromCashBox(getItem(getAdapterPosition()))
//                                            .subscribeOn(Schedulers.io())
//                                            .observeOn(AndroidSchedulers.mainThread())
//                                            .subscribe(() -> Toast.makeText(requireContext(),
//                                                    "Participant deleted from CashBox", Toast.LENGTH_SHORT).show())
//                            )).show();
//                    return true;
                    return false;
                }
            }
        }
    }
}
