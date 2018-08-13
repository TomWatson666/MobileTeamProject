package team5project.treasurehuntapp;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import static team5project.treasurehuntapp.DataVault.animationSmoothnessMultiplier;
import static team5project.treasurehuntapp.DataVault.locationCount;

/**
 * Created by tomwa on 11/03/2017.
 */

@SuppressWarnings("deprecation")
public class TeamTrackerProgressFragment extends Fragment {

    //SELECT `Treasure Hunt`.Location_Count, Team.Progress FROM `Treasure Hunt`, Team WHERE Team.Treasure_Hunt_ID = `Treasure Hunt`.Treasure_Hunt_ID AND Team.Team_Name = 'Team 5';

    public static int selectedTeam = 0;
    public String selectedTeamName = "";
    public static TextView teamNameTextView;
    private static TextView progressTextView;
    private static int progress = 0;
    private boolean isTreasureHuntActive;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.team_tracker_fragment_progress,
                container, false);

        isTreasureHuntActive = !locationCount.equals("");

        if(!selectedTeamName.equals("")) {
            for (selectedTeam = 0; selectedTeam < DataVault.teamNames.size(); selectedTeam++) {

                if (selectedTeamName.equals(DataVault.teamNames.get(selectedTeam)))
                    break;

            }
        }

        teamNameTextView = (TextView) view.findViewById(R.id.team_name_text_view);
        teamNameTextView.setText(isTreasureHuntActive ? DataVault.teamNames.get(selectedTeam) : "No Treasure Hunt Active");

        //The following calculate the progress bar before and after, and create an animation for it
        Resources res = getResources();
        Drawable progressBarDrawable = res.getDrawable(R.drawable.progress_circle);
        final ProgressBar teamTrackerProgressBar = (ProgressBar) view.findViewById(R.id.team_tracker_progress_bar);

        int before = progress;
        progress = Integer.parseInt(DataVault.teamProgress.get(selectedTeam));

        teamTrackerProgressBar.setProgressDrawable(progressBarDrawable);

        teamTrackerProgressBar.setMax(isTreasureHuntActive ? Integer.parseInt(locationCount) * animationSmoothnessMultiplier : 1);

        teamTrackerProgressBar.setSecondaryProgress(isTreasureHuntActive ? Integer.parseInt(locationCount) * animationSmoothnessMultiplier : 1);

        if(progress != before && isTreasureHuntActive) {
            ObjectAnimator progressTransition = ObjectAnimator.ofInt(teamTrackerProgressBar, "progress",
                    before * animationSmoothnessMultiplier, progress * animationSmoothnessMultiplier);
            progressTransition.setDuration(100 * (Math.abs(progress - before)));
            progressTransition.setInterpolator(new DecelerateInterpolator());
            progressTransition.start();
        } else
            teamTrackerProgressBar.setProgress(!isTreasureHuntActive ? 0 : progress * animationSmoothnessMultiplier);


        progressTextView = (TextView) view.findViewById(R.id.progress_text_view);
        progressTextView.setText(isTreasureHuntActive ? DataVault.teamProgress.get(selectedTeam) + "/" + locationCount : "N/A");

        return view;

    }

    public static TeamTrackerProgressFragment newInstance(int index, String teamName) {

        TeamTrackerProgressFragment t = new TeamTrackerProgressFragment();

        Bundle args = new Bundle();
        args.putInt("index", index);

        t.setArguments(args);

        t.selectedTeamName = teamName;

        return t;

    }

    public int getShownIndex() {

        try {
            return getArguments().getInt("index", 0);
        }
        catch(NullPointerException npe) {
            return -1;
        }

    }

}
