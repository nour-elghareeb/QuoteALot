package in.thetechguru.quote.quotealot.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.thetechguru.quote.quotealot.R;
import in.thetechguru.quote.quotealot.viewmodel.POJO.Quote;
import in.thetechguru.quote.quotealot.viewmodel.POJO.SavedQuote;
import in.thetechguru.quote.quotealot.viewmodel.QuoteViewModel;

/**
 * Created by AB on 6/22/2017.
 */

public class AdapterQuotesOnline extends RecyclerView.Adapter<AdapterQuotesOnline.MyViewHolder>{
    private LayoutInflater inflater;
    private List<Quote> quoteItems;
    private Context context;
    private Typeface type;
    private QuoteViewModel quoteViewModel;

    public AdapterQuotesOnline(Context context){
        this.quoteItems=new ArrayList<>();
        inflater= LayoutInflater.from(context);
        this.context = context;
        type = Typeface.createFromAsset(context.getAssets(),"fonts/ga.ttf");
    }

    public void setModel(final QuoteViewModel quoteViewModel){
        this.quoteViewModel=quoteViewModel;
    }

    @Override
    public AdapterQuotesOnline.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_quote_online, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterQuotesOnline.MyViewHolder holder, int position) {
        holder.quote.setText(quoteItems.get(position).getQuote());
        holder.author.setText(quoteItems.get(position).getAuthor());

        if(quoteItems.get(position).isSaved()){
            holder.save.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_star_black_24dp));
        }else {
            holder.save.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_star_border_black_24dp));
        }

        if(quoteItems.get(holder.getPosition()).isSaved()){
            holder.itemView.findViewById(R.id.circularReveal).setVisibility(View.VISIBLE);
        }else {
            holder.itemView.findViewById(R.id.circularReveal).setVisibility(View.INVISIBLE);
        }

        //just a fancy animation, nothing else
        setFadeAnimation(holder.itemView);
    }

    private void setFadeAnimation(View view) {
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
       //AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(300);
        view.startAnimation(anim);
    }

    @Override
    public int getItemCount() {
        return quoteItems.size();
    }

    public void insertItems(List<Quote> quotes){
        //add items at the top
        for(int i=0;i<quotes.size();i++){
            quoteItems.add(0,quotes.get(i));
        }
        notifyItemRangeInserted(0,quotes.size()-1);
    }

    public void clearItems(){
        quoteItems.clear();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView quote, author;
        ImageView save, share;
        View itemView;

        MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            quote = (TextView)itemView.findViewById(R.id.quote);
            author = (TextView)itemView.findViewById(R.id.author);
            save = (ImageView) itemView.findViewById(R.id.buttonSave);
            save.setOnClickListener(this);

            share = (ImageView) itemView.findViewById(R.id.buttonShare);
            share.setOnClickListener(this);

            quote.setTypeface(type);
            author.setTypeface(type);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.buttonSave:

                    if(quoteItems.get(getPosition()).isSaved()){
                        quoteItems.get(getPosition()).setSaved(false);
                        quoteViewModel.deleteQuote(new SavedQuote(quoteItems.get(getPosition())));
                        save.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_star_border_black_24dp));
                    }else {
                        save.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_star_black_24dp));
                        //to make sure online fragment known which quote is already saved
                        quoteItems.get(getPosition()).setSaved(true);
                        //cacheOnDB quote in different db
                        quoteViewModel.saveQuote(quoteItems.get(getPosition()));
                    }

                    circularReveal();

                    break;

                case R.id.buttonShare:
                    //share the freaking thing
                    String shareString = getSharableString(quoteItems.get(getPosition()));
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Quote");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareString);
                    context.startActivity(Intent.createChooser(sharingIntent, "Quote share!"));
                    break;
            }

        }


        private String getSharableString(Quote quote){
            return quote.getQuote() + "\n" + quote.getAuthor() + "\n" + "Shared via QuoteALot!";
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private void circularReveal(){
            if(quoteItems.get(getPosition()).isSaved()) {
                View myView = itemView.findViewById(R.id.circularReveal);
                int cx = myView.getWidth() / 2;
                int cy = myView.getHeight() / 2;
                float finalRadius = (float) Math.hypot(cx, cy);
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
                myView.setVisibility(View.VISIBLE);
                anim.start();
            }else {
                // previously visible view
                final View myView = itemView.findViewById(R.id.circularReveal);

                int cx = myView.getWidth() / 2;
                int cy = myView.getHeight() / 2;
                float initialRadius = (float) Math.hypot(cx, cy);
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        myView.setVisibility(View.INVISIBLE);
                    }
                });
                anim.start();
            }
        }
    }
}
