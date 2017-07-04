package io.github.andreybgm.gigreminder.screen.concerts;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Concert;

public class ListViewHolder extends RecyclerView.ViewHolder {

    private String thisYearDateFormat;
    private String anotherYearDateFormat;

    @BindView(R.id.text_artist)
    TextView artistTextView;

    @BindView(R.id.text_date)
    TextView dateTextView;

    @BindView(R.id.text_place)
    TextView placeTextView;

    public ListViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);

        thisYearDateFormat = DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMdEEE");
        anotherYearDateFormat = DateFormat.getBestDateTimePattern(Locale.getDefault(),
                "yyyyMMMdEEE");
    }

    public void bind(Concert concert) {
        artistTextView.setText(concert.getArtist().getName());
        placeTextView.setText(makePlaceString(concert));
        dateTextView.setText(makeDateString(concert));
    }

    private String makePlaceString(Concert concert) {
        return String.format("%s, %s", concert.getLocation().getName(), concert.getPlace());
    }

    private CharSequence makeDateString(Concert concert) {
        int currentYear = new GregorianCalendar().get(Calendar.YEAR);

        GregorianCalendar concertCalendar = new GregorianCalendar();
        concertCalendar.setTime(concert.getDate());
        int concertYear = concertCalendar.get(Calendar.YEAR);

        String dateFormatString =
                currentYear == concertYear ? thisYearDateFormat : anotherYearDateFormat;

        return DateFormat.format(dateFormatString, concert.getDate());
    }
}
